/**
 * Copyright (c) 2022 Mauro Trevisan
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mtrevisan.mapmatcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;


public class FileReplace{

	public static void main(String[] args){
		FileReplace fr = new FileReplace();
		fr.convertTimestamp(";", 3);
	}


	private void convertTimestamp(String separator, int index){
		ZoneOffset offset = ZoneOffset.UTC;
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

		File fin = new File("C:\\mauro\\mine\\projects\\MapMatcher\\src\\test\\resources\\positions.csv");
		File fout = new File("C:\\mauro\\mine\\projects\\MapMatcher\\src\\test\\resources\\positions-out.csv");

		try(
				BufferedReader in = new BufferedReader(new FileReader(fin));
				BufferedWriter out = new BufferedWriter(new FileWriter(fout));
			){
			String line;
			while((line = in.readLine()) != null){
				String[] cells = line.split(separator);

				if(cells.length > 0){
					long epocMillis = Long.parseLong(cells[index]);
					long epochSecond = epocMillis / 1000;
					int nanoOfSecond = (int)(epocMillis - epochSecond * 1000) * 1_000_000;
					cells[index] = dateTimeFormatter.format(LocalDateTime.ofEpochSecond(epochSecond, nanoOfSecond, offset));

					line = String.join(separator, Arrays.asList(cells));
					out.write(line);
					out.write("\r\n");
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
