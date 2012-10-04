package com.res.java.lib;
/**
 * 
 */

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * StringTokenizer with the delimiters included if requested. <br />
 * Better than:
 * <ul>
 * <li>StringTokenizer because it can take a regexp and not a just a collection of characters</li>
 * <li>String.split() because:
 * <ul>
 * <li>it can include delimiters if needed</li>
 * <li>if there is not match, return an empty iterator, rather than an array with one empty String in it</li>
 * </ul>
 * </ul>
 * In response of a <a href="http://stackoverflow.com/questions/275768#275860">StackOverflow code challenge</a>
 * @author <a href="http://stackoverflow.com/users/6309/vonc">VonC</a>
 */
public class StringTokenizerEx implements Iterator<String>, Enumeration<String>, Iterable<String>
{

	/**
	 * Illustrates the different possible results of StringTokenizerEx. <br />
	 * Limit cases are included (null or empty string, null or empty regexp, non-matching regexp...)
	 * @param args ignored
	 */
	public static void main(final String[] args) {
		System.out.println("StringTokenizerEx usages:");

		System.out.println(new StringTokenizerEx(null, null));
		System.out.println(new StringTokenizerEx("", null));
		System.out.println(new StringTokenizerEx(null, ""));
		System.out.println(new StringTokenizerEx("", ""));
		System.out.println(new StringTokenizerEx("abcd", "ab"));
		System.out.println(new StringTokenizerEx("abcd", "cd"));
		System.out.println(new StringTokenizerEx("abcd", "abcd"));
		System.out.println(new StringTokenizerEx("abcd", "bc"));
		System.out.println(new StringTokenizerEx("abcd \t efg  hi   j"));
		System.out.println(new StringTokenizerEx("'ab','cd','eg'","\\W+"));		
		System.out.println(new StringTokenizerEx("boo:and:foo",":"));
		System.out.println(new StringTokenizerEx("boo:and:foo","o"));
		System.out.println(new StringTokenizerEx("boo:and:foo","o+"));
	}
	private int index = 0;
	private int length = -1;
	private String stringToSplit = null;
	private String regexp = null;
	private String[] values = new String[] {};
	private String[] delimiters = new String[] {};
	private static final  String DEFAULT_REGEXP = "[ \\t\\n\\r\\f]+";
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
		final StringBuilder aStringTokenizerExString = new StringBuilder("'" + this.stringToSplit+"'");
		aStringTokenizerExString.append(" to be splitted with regexp '").append(this.regexp + "' gives ");
		aStringTokenizerExString.append("[").append(this.getDelimiter()).append("]");
		int anOldIndex = this.index;
		this.index = 0;
		for (String aString : this) 
		{
			aStringTokenizerExString.append(", '").append(aString).append("', [").append(getDelimiter()).append("]");
		}
		this.index = anOldIndex;
		return aStringTokenizerExString.toString();
	}
	/**
	 * Split a String with default regexp "[ \t\n\r\f]+". <br />
	 * Delimiters are stored
	 * @param aStringToSplit string to split, can be null or empty (meaning, empty collection, no delimiters)
	 */

	public StringTokenizerEx(final String aStringToSplit)
	{
		this.stringToSplit = aStringToSplit;
		this.regexp = DEFAULT_REGEXP;
		split();
	}
	
	/**
	 * Split a String with given regexp. <br />
	 * Delimiters are stored
	 * @param aStringToSplit string to split, can be null or empty (meaning, empty collection, no delimiters)
	 * @param aRegExp a regular expression, can be null or empty, meaning empty collection, one delimiter: aString itself
	 */
	public StringTokenizerEx(final String aStringToSplit, final String aRegExp)
	{
		this.stringToSplit = aStringToSplit;
		this.regexp = aRegExp;
		split();
	}
	
	/**
     * Tests if there are more strings between regexp matches available from this tokenizer's string. <br />
     * If this method returns <tt>true</tt>, then a subsequent call to <tt>nextElement</tt> with no argument 
     * will successfully return a String.
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	@Override
	public final boolean hasMoreElements() 
	{
		return this.index < this.length;
	}
	
	/**
	 * @see #hasMoreElements()
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public final boolean hasNext() {
		return hasMoreElements();
	}
	
	/**
     *
     * @return     the next token from this string tokenizer.
     * @exception  NoSuchElementException  if there are no more tokens in this tokenizer's string.
     */
	/**
     * Returns the next String from this string tokenizer if the regexp has match anything. <br />
     * Otherwise, no element is available, but the delimiters will contain only one element: the original String itself.
	 * @see java.util.Enumeration#nextElement()
	 */
	@Override
	public final String nextElement() 
	{
		final String ares = this.values[this.index];
    	this.index = this.index + 1;
		return ares;
	}
	
	/**
	 * @see #nextElement()
	 * @see java.util.Iterator#next()
	 */
	@Override
	public final String next() {
		return nextElement();
	}
	
	/**
	 * Get current delimiter before the next split string detected. <br />
	 * Based on the number of time {@link #nextElement()} has been called. <br />
	 * @return delimiter (never null), can be empty if there is no next element, or if there was no match before the first element
	 */
	public final String getDelimiter() 
	{
		String ares = "";
		if(this.index < this.delimiters.length) 
		{
			ares = this.delimiters[this.index];
		}
		return ares;
	}

	private void split()
	{
		this.length = 0;
		if(isEmptyString(this.stringToSplit))
		{
			// no delimiter, no element in this collection
			return;
		}
		if(isEmptyString(this.regexp))
		{
			// one delimiter, no element in this collection
			this.delimiters = new String[] { this.stringToSplit };
			return;
		}
		final Pattern aPattern = Pattern.compile(this.regexp);
		final Matcher aMatcher = aPattern.matcher(this.stringToSplit);
		if(aMatcher.find() == false)
		{
			// no match, means only one delimiter, and empty collection
			this.delimiters = new String[] { this.stringToSplit };
			return;
		}
		aMatcher.reset();
		final ArrayList<String> someDelimiters = new ArrayList<String>();
		final ArrayList<String> someValues = new ArrayList<String>();
		int lastEnd = 0;
		while(aMatcher.find())
		{
			final String aDelimiter = this.stringToSplit.substring(aMatcher.start(), aMatcher.end());
			if(aMatcher.start() > lastEnd)
			{
				someValues.add(this.stringToSplit.substring(lastEnd, aMatcher.start()));
				if(lastEnd == 0)
				{
					someDelimiters.add("");
				}
				someDelimiters.add(aDelimiter);
			}
			else
			{
				if(lastEnd > 0)
				{
					someValues.add("");
				}
				someDelimiters.add(aDelimiter);
			}
			lastEnd = aMatcher.end();
		}
		if(lastEnd < this.stringToSplit.length())
		{
			someValues.add(this.stringToSplit.substring(lastEnd));
		}
		this.delimiters = someDelimiters.toArray(this.delimiters);
		this.values = someValues.toArray(this.values);
		this.length = someValues.size();
	}
	
	/**
	 * <b>NOT SUPPORTED operation</b>. <br />
	 * Iteration only, no list modification for a Tokenizer.
	 */
	@Override
	public final void remove() 
	{
		throw new UnsupportedOperationException();
	}
	
	private static boolean isEmptyString(final String aString)
	{
		return aString == null || aString.length() == 0;
	}
	/**
	 * Returns itself as an Iterator. <br />
	 * Allows for a quick iteration over the splitted Strings and the delimiters.
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public final Iterator<String> iterator() {
		return this;
	}
}