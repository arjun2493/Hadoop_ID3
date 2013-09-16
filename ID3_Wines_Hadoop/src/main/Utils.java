/*
* Hadoop ID3
* Copyright (C) 2013 George Piskas
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along
* with this program; if not, write to the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* Contact: geopiskas@gmail.com
*/

package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import wine.QualityStats;

public class Utils {
	
	// The content of the given file is converted to a single line.
	public static void toSingleLine(String in) throws IOException {
		File fileIn = new File(in);
		File fileInCP = new File(in);
		BufferedReader input = new BufferedReader(new FileReader(fileIn));
		File fileOut = new File("tmp");
		BufferedWriter output = new BufferedWriter(new FileWriter(fileOut));
		String line = null;
		while ((line = input.readLine()) != null) {
			output.write(line.replaceAll("\\s+"," ") + " ");
		}
		output.close();
		input.close();
		fileIn.delete();
		fileOut.renameTo(fileInCP);
	}

	// Combines all leaves files from each MapReduce phase.
	public static void combineLeaves(FileSystem fs, int fold, int mrPhase) throws IOException {
		String line;
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(fs.create( new Path(Main.TRAIN_OUTPUT_PATH + Main.FOLD_DIR_PREFIX + fold + Main.LEAVES_PREFIX))));
		
		for (int i = 0; i <= mrPhase; i++) {
			BufferedReader input = new BufferedReader(new InputStreamReader(fs.open( new Path(Main.TRAIN_OUTPUT_PATH + Main.FOLD_DIR_PREFIX + fold + Main.PHASE_DIR_PREFIX + i + "/part-r-00000"))));
			while ((line = input.readLine()) != null) {
				output.write(line + "\n");
			}
			input.close();
		}
		output.close();
	}

	// Calculates log2 of a given value x.
	public static float log2(float x) {
		if (x == 0) {
			return 0;
		} else {
			return (float) (Math.log(x) / Math.log(2));
		}
	}
	
	// Calculates the information gain the given quality stats array.
	public static float infoGain(QualityStats[] qualities, float initEnt) {
		int total = 0;
		for(QualityStats q : qualities) {
			total += q.getWineCount();
		}
		
		float gain = initEnt;
		for(QualityStats q : qualities) {
			gain -= (float)q.getWineCount() / total * q.getEntropy();
		}
		return gain;
	}
}
