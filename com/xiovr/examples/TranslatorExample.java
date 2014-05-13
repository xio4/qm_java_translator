/*
 * Copyright (C) 2014 Shalkov Petr
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
package com.xiovr.examples;
import com.xiovr.lang.Translator;
import static com.xiovr.lang.Translator.tr;
import java.util.Locale;
import java.io.*;

public class TranslatorExample
{
	private static final String EN_FN_PATH			= "en_US.qm";
	private static final String RU_FN_PATH			= "ru_RU.qm";
	private static final String CH_FN_PATH			= "ch_CH.qm";

	public static void showText()
	{
		System.out.println(tr("Hello habr!"));
		System.out.println(tr("Every will be fine!"));
	}

	public static Translator createTranslator(String filePath)
	{
		Translator translator = new Translator();
		try {
			File file = new File(filePath);	
			InputStream is = new FileInputStream(file);
			byte[] data = new byte[(int)file.length()];
			is.read(data);
			is.close();
			boolean loadStatus = translator.load(data);
			if	(!loadStatus)
				System.out.println("Error load translate file " + filePath);

		}	catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}		
		return translator;

	}

	public static void main(String[] argc)
	{
		Translator trEn = createTranslator(EN_FN_PATH);
		Translator trRu = createTranslator(RU_FN_PATH);
		Translator trCh = createTranslator(CH_FN_PATH);
		Translator trLoc = createTranslator(Locale.getDefault().toString()+".qm");
		System.out.println("Translator example");
		System.out.println("\n======= Chinese ========");
		Translator.activate(trCh);
		showText();
		System.out.println("\n======= English ========");
		Translator.activate(trEn);
		showText();
		System.out.println("\n======== Locale ========");
		Translator.activate(trLoc);
		showText();
		System.out.println("\n======= Russian ========");
		Translator.activate(trRu);
		showText();
	}
}
