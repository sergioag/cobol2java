package com.res.java.translation.engine;

import com.res.cobol.Main;
import java.util.ArrayList;
import java.util.Enumeration;

import com.res.cobol.syntaxtree.AlternateRecordKeyClause;
import com.res.cobol.syntaxtree.CompilationUnit;
import com.res.cobol.syntaxtree.DataDivision;
import com.res.cobol.syntaxtree.DataDivisionSection;
import com.res.cobol.syntaxtree.DataName;
import com.res.cobol.syntaxtree.FileAndSortDescriptionEntry;
import com.res.cobol.syntaxtree.FileAndSortDescriptionEntryClause;
import com.res.cobol.syntaxtree.FileControlClause;
import com.res.cobol.syntaxtree.FileName;
import com.res.cobol.syntaxtree.FileSection;
import com.res.cobol.syntaxtree.KeyClause;
import com.res.cobol.syntaxtree.NestedProgramIdParagraph;
import com.res.cobol.syntaxtree.NestedProgramUnit;
import com.res.cobol.syntaxtree.Node;
import com.res.cobol.syntaxtree.NodeSequence;
import com.res.cobol.syntaxtree.NodeToken;
import com.res.cobol.syntaxtree.ProgramIdParagraph;
import com.res.cobol.syntaxtree.ProgramUnit;
import com.res.cobol.syntaxtree.QualifiedDataName;
import com.res.cobol.visitor.DepthFirstVisitor;
import com.res.common.RESConfig;
import com.res.java.lib.Constants;
import com.res.java.lib.RunTimeUtil;
import com.res.java.translation.symbol.SymbolConstants;
import com.res.java.translation.symbol.SymbolProperties;
import com.res.java.translation.symbol.SymbolTable;
import com.res.java.util.ClassFile;
import com.res.java.util.ClassFileWriter;
import com.res.java.util.DBGenClassFileWriter;

public class VsamIsam2DbGen extends DepthFirstVisitor {

    private static VsamIsam2DbGen thiz = null;

    private VsamIsam2DbGen() {
    }

    public static VsamIsam2DbGen getInstance() {
        if (thiz == null) {
            thiz = new VsamIsam2DbGen();
        }
        return thiz;
    }

    public static void clear() {
        thiz = null;
    }
    private SymbolProperties props = null;
    private ClassFileWriter classFile = null;
    private ArrayList<SymbolProperties> files2Gen = new ArrayList<SymbolProperties>();
    private String lastDataName = null;

    @Override
    public void visit(CompilationUnit n) {
        super.visit(n);
    }

    @Override
    public void visit(NestedProgramUnit n) {
        n.nestedIdentificationDivision.accept(this);
        n.nodeOptional1.accept(this);
        //n.nodeOptional.accept(this);
        //n.nodeOptional2.accept(this);
        SymbolTable.getScope().endProgram();
    }

    @Override
    public void visit(ProgramUnit n) {
        n.identificationDivision.accept(this);
        initializePass();
        n.nodeOptional1.accept(this);
        //n.nodeOptional.accept(this);
        //n.nodeOptional2.accept(this);
        SymbolTable.getScope().endProgram();
        endPass();
    }

    private void initializePass() {
        if (classFile == null) {
            ClassFile.doProgramScope(classFile = new DBGenClassFileWriter(
                    SymbolTable.getScope().getFirstProgram().getDataName(), "db_gen", false));
            printSymbol = new PrintSymbol(true);
        }
    }

    private void endPass() {
        if (classFile != null) {
            classFile.backTab();
            classFile.println("}");
        }
    }

    @Override
    public void visit(ProgramIdParagraph n) {
        props = SymbolTable.getScope().lookup(n.programName.cobolWord.nodeToken.tokenImage, SymbolConstants.PROGRAM);
        if (props == null) {
            RunTimeUtil.getInstance().reportError("Fatal Error: Cobol2Java.visit(ProgramIdParagraph):"
                    + n.programName.cobolWord.nodeToken.tokenImage, true);
        }
        SymbolTable.getScope().startProgram(props);
    }

    @Override
    public void visit(NestedProgramIdParagraph n) {
        props = SymbolTable.getScope().lookup(n.programName.cobolWord.nodeToken.tokenImage, SymbolConstants.PROGRAM);
        if (props == null) {
            reportError("Fatal Error: Cobol2Java.visit(ProgramIdParagraph):"
                    + n.programName.cobolWord.nodeToken.tokenImage);
        }
        SymbolTable.getScope().startProgram(props);
    }

    @Override
    public void visit(FileSection n) {
        ClassFile.tab();
        if (n.nodeChoice.which == 1) {
            NodeSequence nodeseq = (NodeSequence) n.nodeChoice.choice;
            nodeseq.elementAt(0).accept(this);
            if (props.getDataUsage() == Constants.INDEXED) {
                doOneFile();
            }
        }
        if (n.nodeListOptional.present()) {
            for (Enumeration<Node> e = n.nodeListOptional.elements(); e.hasMoreElements();) {
                NodeSequence nodeseq = (NodeSequence) e.nextElement();
                nodeseq.elementAt(0).accept(this);
                if (props.getDataUsage() != Constants.INDEXED) {
                    //reportError("Skipped file "+props.getDataName()+". Not Indexed Organization.",false);
                    continue;
                }
                doOneFile();
            }
        }

        ClassFile.println("public static void main(String[] args) throws IOException, SQLException {");
        ClassFile.tab();
        ClassFile.println(((DBGenClassFileWriter) ClassFile.current).getClassName()
                + " instance = new "
                + ((DBGenClassFileWriter) ClassFile.current).getClassName() + "();");
        ClassFile.println("instance.execute();");
        ClassFile.backTab();
        ClassFile.println("}");
        ClassFile.println("public void execute() throws IOException, SQLException {");
        ClassFile.tab();
        for (SymbolProperties file : files2Gen) {
            ClassFile.println("do" + file.getJavaName2() + "();");
        }
        ClassFile.backTab();
        ClassFile.println("}");
        int saveOpt = RESConfig.getInstance().getOptimizeAlgorithm();
        RESConfig.getInstance().setOptimizeAlgorithm(0);
        //Dump all data records at the end.
        for (SymbolProperties file : files2Gen) {
            SymbolTable.visit(file.getChildren().get(0), printSymbol);
        }
        RESConfig.getInstance().setOptimizeAlgorithm(saveOpt);
    }

    private void doOneFile() {
        if (props.getChildren() == null || props.getChildren().size() <= 0) {
            reportError("Skipped file " + props.getDataName() + ". Does not include data record.", false);
        }
        reportError("Generating DB for Indexed file " + props.getDataName() + ".", false);
        files2Gen.add(props);
        //Create a COBOL Sequential props to read REPROed data-props
        ClassFile.println("public CobolFile "
                + props.getJavaName1()
                + " = new CobolFile(" + props.getOtherName() + ");");
        String fileName = props.getJavaName1();
        //Construct DROP TABLE...,CREATE TABLE..., INSERT INTO... SQL.
        //String dropSql = TranUtil.get().getSQLDrop(props);
        String createSql = TranUtil.get().getSQLCreate(props, "");
        //String insertSql=TranUtil.get().getSQLInsert(props);
        ClassFile.println("private static final String " + fileName + "_create_SQL = "
                + Main.getContext().getCobol2Java().formatLiteral(createSql) + ";");
        //ClassFile.println("private static final String "+fileName+"_insert_SQL = "+
        //Main.getContext().getCobol2Java().formatLiteral(insertSql)+";");
        //Generate methods that are called from DbMigrate
        ClassFile.doMethodScope("public boolean do" + props.getJavaName2() + "() throws IOException, SQLException{");
        ClassFile.tab();
        SymbolProperties dataRec = props.getChildren().get(0);
        ClassFile.println(props.getJavaName1() + ".openInput();");
        ClassFile.println("int bytesRead=0,recordsRead=0,rowsCommitted=0;");
        ClassFile.println("try {");
        ClassFile.tab();
        //ClassFile.println("__dao().prepareStatement("+Main.getContext().getCobol2Java().formatLiteral(dropSql)+");");
        ClassFile.println("__dao().executeStatement();");
        ClassFile.backTab();
        ClassFile.println("} catch(SQLException e) {}");
        ClassFile.println("__dao().prepareStatement(" + fileName + "_create_SQL);");
        ClassFile.println("__dao().executeStatement();");
        ClassFile.println("__dao().prepareStatement(" + fileName + "_insert_SQL);");
        ClassFile.println("do {");
        ClassFile.println("\tbytesRead=" + props.getJavaName1() + ".read(" + dataRec.getJavaName1() + ".get());");
        ClassFile.println("\tif (bytesRead<=0) break;");
        ClassFile.println("\tif (bytesRead<" + String.valueOf(dataRec.getLength()) + "){ System.out.println(\"Partial record(\"+bytesRead+\" bytes) ignored.\");break; }");
        ClassFile.println("\trecordsRead++;");
        StringBuffer line = new StringBuffer();
        TranUtil.get().setSQLParameters(line);
        ClassFile.println("__dao().executeStatement();");
        ClassFile.println("} while(true);");
        ClassFile.println("__dao().prepareStatement(\"select count(*) from " + props.getJavaName1() + "\");");
        ClassFile.println("__dao().executeQuery();rowsCommitted=__dao().getInt(1);");
        ClassFile.println("System.out.println("
                + Main.getContext().getCobol2Java().formatLiteral('\"' + fileName + '\"')
                + "+\" Records Read: \"+recordsRead+\";Rows Committed: \"+rowsCommitted);");
        ClassFile.println("return true;");
        ClassFile.backTab();
        ClassFile.endMethodScope();
    }

    @Override
    public void visit(DataDivision n) {
        super.visit(n);
    }

    @Override
    public void visit(DataDivisionSection n) {
        switch (n.nodeChoice.which) {
            case 0:
                n.nodeChoice.choice.accept(this);
            default:

        }
    }
    private ArrayList<SymbolProperties> alternateKeys = new ArrayList<SymbolProperties>();

    @Override
    public void visit(FileAndSortDescriptionEntry n) {
        n.fileName.accept(this);
        if (props.getDataUsage() != Constants.INDEXED) {
            return;
        }
        alternateKeys.clear();
        n.nodeListOptional.accept(this);
    }

    @Override
    public void visit(NodeToken n) {
    }

    @Override
    public void visit(FileName n) {
        props = SymbolTable.getScope().lookup(n.cobolWord.nodeToken.tokenImage, SymbolConstants.FILE);
        if (props == null) {
            reportError("Unknown File Name: " + n.cobolWord.nodeToken.tokenImage + ". Contact RES support.", false);
        }
    }

    @Override
    public void visit(KeyClause n) {
        n.qualifiedDataName.accept(this);
    }

    @Override
    public void visit(FileAndSortDescriptionEntryClause n) {
        // TODO Auto-generated method stub
        super.visit(n);
    }

    @Override
    public void visit(QualifiedDataName n) {
        props = null;
        n.nodeSequence.accept(this);
        if (lastDataName == null) {
            return;
        }
        props = SymbolTable.getScope().lookup(lastDataName, SymbolConstants.DATA);
        if (props == null) {
            reportError("Unknown Symbol :" + lastDataName);
        }
    }

    @Override
    public void visit(DataName n) {
        lastDataName = n.cobolWord.nodeToken.tokenImage;
    }

    @Override
    public void visit(AlternateRecordKeyClause n) {
        n.qualifiedDataName.accept(this);
        alternateKeys.add(props);
    }

    @Override
    public void visit(FileControlClause n) {
        // TODO Auto-generated method stub
        super.visit(n);
    }

    private void reportError(String msg) {
        RunTimeUtil.getInstance().reportError(msg, true);
    }

    private void reportError(String msg, boolean exit) {
        RunTimeUtil.getInstance().reportError(msg, exit);
    }
    PrintSymbol printSymbol = null;
}
