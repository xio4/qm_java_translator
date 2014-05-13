/*
 * Copyright (C) 2014 Shalkov Petr
 * The file based on sources Qt4.8: http://qt-project.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0

 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */

package com.xiovr.lang;
import java.util.Map;
import java.util.HashMap;
import java.lang.StackTraceElement;
import java.io.UnsupportedEncodingException;
public class Translator 
{
	// Constants
	private static final byte Tag_End				= (byte)0x01;
	private static final byte Tag_SourceText16	= (byte)0x02;
	private static final byte Tag_Translation		= (byte)0x03;
	private static final byte Tag_Context16		= (byte)0x04;
	private static final byte Tag_Obsolete1		= (byte)0x05;
	private static final byte Tag_SourceText		= (byte)0x06;
	private static final byte Tag_Context			= (byte)0x07;
	private static final byte Tag_Comment			= (byte)0x08;
	private static final byte Tag_Obsolete2		= (byte)0x09;

	private static final byte Contexts				= (byte)0x2f;
	private static final byte Hashes					= (byte)0x42;
	private static final byte Messages				= (byte)0x69;
	private static final byte NumerusRules			= (byte)0x88;

	private static final int MAGIC_LENGTH			= 16;
	private static final byte[] magic = new byte[]{
		(byte)0x3c, (byte)0xb8, (byte)0x64, (byte)0x18, 
		(byte)0xca, (byte)0xef, (byte)0x9c, (byte)0x95,
		(byte)0xcd, (byte)0x21, (byte)0x1c, (byte)0xbf, 
		(byte)0x60, (byte)0xa1, (byte)0xbd, (byte)0xdd
	};
	// Character encoding
	private static final String C_ENC				= "UTF-16"; 
	private static Translator activated;

	private Map<String, Map<String,String>> table;

	public Translator()
	{
		table = new HashMap<String, Map<String,String>>();
	}

	public Map<String, Map<String, String>> getTable()
	{
		return table;
	}

	public static String tr(String sourceText)
	{
		if (activated == null)
			return sourceText;
		
		// Get context from StackTrace
		StackTraceElement[] steArray = Thread.currentThread().getStackTrace();
		String context = steArray[steArray.length-1].getClassName();
		Map<String, String> map = activated.getTable().get(context);
		if (map != null) {
			String translate = map.get(sourceText);
			if (translate != null)
				return translate;
		}
		return sourceText;	
	}
	public static void activate(Translator trans)
	{
		activated = trans;
	}

	private int read32(byte[] data, int pos)
	{
		return (int)(((data[pos] & 0xFF) << 24) 
			| ((data[pos+1] & 0xFF) << 16)
			| ((data[pos+2] & 0xFF) << 8)
			| ((data[pos+3] & 0xFF)));
	}

	public boolean load(byte[] data)
	{

		if ((data == null) || (data.length < MAGIC_LENGTH))
			return false;
		
		// Check magic numbers
		for (int i=0; i < MAGIC_LENGTH; ++i) {
			if (data[i] != magic[i])
				return false;
		}


		int startPos = MAGIC_LENGTH;
		int len = data.length;
		int blockLen = 0;
		byte tag = 0;
		int contextPos = 0;
		int contextLength = 0;
		int offsetPos = 0;
		int offsetLength = 0;
		int messagePos = 0;
		int messageLength = 0;
		int numerusRulesPos = 0;
		int numerusRulesLength = 0;

		while (startPos < len - 4) {
			tag = data[startPos++];
			blockLen = read32(data, startPos);
			
			startPos += 4;
			if ((tag == 0) || (blockLen == 0) || (startPos + blockLen > len)) {
				return false;
			}
			
			if (tag == Contexts) {
				contextPos = startPos;
				contextLength = blockLen;
			} else if (tag == Hashes) {
				offsetPos = startPos;
				offsetLength = blockLen;
			} else if (tag == Messages) {
				messagePos = startPos;
				messageLength = blockLen;
			} else if (tag == NumerusRules) {
				numerusRulesPos = startPos;
				numerusRulesLength = blockLen;
			}
			startPos += blockLen;

		}

		String tn = "";
		String sourceText = "";
		String comment = "";
		String context = "";
		int el_len = 0;
		int pos = messagePos;
		int end = pos + messageLength;
		Map<String, String> map;

		for (;;) {
			tag = 0;

			if (pos < end) 
				tag = data[pos++];
			else break;

			switch (tag) {
			case Tag_End:
				if (table.containsKey(context)) {
					table.get(context).put(sourceText, tn);
				}
				else {
					map = new HashMap<String, String>();
					map.put(sourceText, tn);
					table.put(context, map);
				}
				break;
			case Tag_Translation: {
				el_len = read32(data, pos);	
				if ((el_len % 1) > 0)
					return false;
				pos += 4;
				try {
					tn = new String(data, pos, el_len, C_ENC);
				} catch (UnsupportedEncodingException e) {
					return false;
				}
				pos += el_len;
				break;
			}
			case Tag_Obsolete1: 
				pos += 4;
				break;
			case Tag_SourceText: {
				el_len = read32(data, pos);
				pos += 4;
				sourceText = new String(data, pos, el_len);
				pos += el_len;
				break;
			}
			case Tag_Context: {
				el_len = read32(data, pos);
				pos += 4;
				context = new String(data, pos, el_len);
				pos += el_len;
				break;
			}
			case Tag_Comment: {
				el_len = read32(data, pos);
				pos += 4;
				comment = new String(data, pos, el_len);
				pos += el_len;
				break;
			}
			default:
				break;
			}
		}
		return true;
	}
}
