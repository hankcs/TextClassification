/**
 * Copyright 2009 DigitalPebble Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.digitalpebble.classification.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import liblinear.Model;

import com.digitalpebble.classification.Lexicon;

public class ModelUtils {

	/** 
	 * Prints out the attributes and their weights from the models generated by liblinear.
	 * This is different from CorpusUtils.dumpBestAttributes which computes a score for the attributes
	 * regardless of the model.
	 **/

	public static void getAttributeScores(String modelPath, String lexiconF)
			throws IOException {
		// load the model + the lexicon
		// try to see if we can get a list of the best scores from the model
		// works only for liblinear
		Lexicon lexicon = new Lexicon(lexiconF);
		Model liblinearModel = Model.load(new File(modelPath));
		double[] weights = liblinearModel.getFeatureWeights();
		// dump all the weights
		int numClasses = liblinearModel.getNrClass();
		int numFeatures = liblinearModel.getNrFeature();

		Map<Integer, String> invertedAttributeIndex = lexicon
				.getInvertedIndex();

		for (int i = 0; i < weights.length; i++) {
			// get current class num
			int classNum = i / numFeatures;
			int featNum = i % numFeatures;
			String classLabel = lexicon.getLabel(classNum);
			String attLabel = invertedAttributeIndex.get(featNum + 1);

			// display the values between -0.001 and +0.001 as 0
			if (weights[i] < 0.001 && weights[i] > -0.001)
				weights[i] = 0;

			System.out
					.println(attLabel + "\t" + classLabel + "\t" + weights[i]);
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("ModelUtils : \n");
			buffer.append("\t -getAttributeScores modeFile lexicon\n");
			System.out.println(buffer.toString());
			return;
		}

		else if (args[0].equalsIgnoreCase("-getAttributeScores")) {
			String model = args[1];
			String lexicon = args[2];
			try {
				getAttributeScores(model, lexicon);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
