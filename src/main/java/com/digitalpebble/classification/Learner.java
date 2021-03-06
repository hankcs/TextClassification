/**
 * Copyright 2009 DigitalPebble Ltd
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.digitalpebble.classification;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.digitalpebble.classification.liblinear.LibLinearModelCreator;
import com.digitalpebble.classification.libsvm.LibSVMModelCreator;
import com.digitalpebble.classification.util.scorers.AttributeScorer;
import com.digitalpebble.classification.util.scorers.logLikelihoodAttributeScorer;

/**
 * 学习器
 */
public abstract class Learner
{
    /**
     * 词表
     */
    protected Lexicon lexicon;

    /**
     * 词表存放位置
     */
    protected String lexiconLocation;

    /**
     * 参数
     */
    protected String parameters;

    /**
     * 工作路径
     */
    protected File workdirectory;

    private int keepNBestAttributes = -1;

    /**
     * SVM
     */
    public static final String LibSVMModelCreator = "LibSVMModelCreator";

    /**
     * Linear
     */
    public static final String LibLinearModelCreator = "LibLinearModelCreator";

    /**
     * 设置权重计算方法 Specify the method used for building a vector from a document *
     *
     * @param method
     */
    public void setMethod(Parameters.WeightingMethod method)
    {
        this.lexicon.setMethod(method);
    }

    /**
     * 是否规范化向量
     * Specify whether or not the vectors have to be normalized *
     */
    public void setNormalization(boolean norm)
    {
        this.lexicon.setNormalizeVector(norm);
    }

    /**
     * 过滤单词
     * This must be called between the creation of the documents and the
     * learning. It keeps only the terms occuring in at least mindocs documents
     * and in a maximum of maxdocs documents.
     *
     * @param minDocs 最低出现在多少文档中
     * @param maxdocs 最高出现在多少文档中
     */
    public void pruneTermsDocFreq(int minDocs, int maxdocs)
    {
        lexicon.pruneTermsDocFreq(minDocs, maxdocs);
    }

    /***************************************************************************
     * Keep only the top n attributes according to their LLR score This must be
     * set before starting the training
     **************************************************************************/
    public void keepTopNAttributesLLR(int rank)
    {
        keepNBestAttributes = rank;
    }

    public Document createDocument(List<Field> fields, String label)
    {
        Field[] fs = (Field[]) fields.toArray(new Field[fields.size()]);
        return createDocument(fs, label);
    }

    public Document createDocument(Field[] fields, String label)
    {
        this.lexicon.incrementDocCount();
        MultiFieldDocument doc = new MultiFieldDocument(fields, this.lexicon,
                                                        true);
        doc.setLabel(this.lexicon.getLabelIndex(label));
        return doc;
    }

    /**
     * Create a Document from an array of Strings
     */
    public Document createDocument(String[] tokenstring)
    {
        this.lexicon.incrementDocCount();
        return new SimpleDocument(tokenstring, this.lexicon, true);
    }

    /**
     * Create a Document from an array of Strings and specify the label
     */
    /**
     * 从一个单词数据创建文档并且指定label
     * @param tokenstring
     * @param label
     * @return
     */
    public Document createDocument(String[] tokenstring, String label)
    {
        this.lexicon.incrementDocCount();
        SimpleDocument doc = new SimpleDocument(tokenstring, this.lexicon, true);
        doc.setLabel(this.lexicon.getLabelIndex(label));
        return doc;
    }

    protected abstract void internal_learn() throws Exception;

    protected abstract void internal_generateVector(TrainingCorpus documents)
            throws Exception;

    protected abstract boolean supportsMultiLabels();

    protected abstract String getClassifierType();

    public void learn(TrainingCorpus corpus) throws Exception
    {
        generateVectorFile(corpus);
        internal_learn();
        // save the lexicon so that we can get the linear weights for the
        // attributes
        this.lexicon.saveToFile(this.lexiconLocation);
    }

    /***************************************************************************
     * do not start the learning but only generates an input file for the
     * learning algorithm. The actual training can be done with an external
     * command.
     *
     * @throws Exception
     **************************************************************************/
    public void generateVectorFile(TrainingCorpus corpus) throws Exception
    {

        if (this.lexicon.getLabelNum() < 2)
        {
            throw new Exception(
                    "There must be at least two different class values in the training corpus");
        }

        // check that the current learner can handle
        // the number of classes
        if (this.lexicon.getLabelNum() > 2)
        {
            if (supportsMultiLabels() == false)
                throw new Exception(
                        "Leaner implementation does not support multiple classes");
        }

        // store in the lexicon the information
        // about the classifier to use
        this.lexicon.setClassifierType(getClassifierType());

        // compute the loglikelihood score for each attribute
        // and remove the attributes accordingly
        if (keepNBestAttributes != -1)
        {
            // double scores[] = logLikelihoodAttributeFilter.getScores(corpus,
            // this.lexicon);
            // this.lexicon.setLogLikelihoodRatio(scores);
            // this.lexicon.keepTopNAttributesLLR(keepNBestAttributes);
            AttributeScorer scorer = logLikelihoodAttributeScorer.getScorer(
                    corpus, lexicon);
            this.lexicon.setAttributeScorer(scorer);
            this.lexicon.applyAttributeFilter(scorer, keepNBestAttributes);
        }
        // saves the lexicon
        this.lexicon.saveToFile(this.lexiconLocation);

        // action specific to each learner implementation
        internal_generateVector(corpus);
    }

    public boolean saveLexicon()
    {
        try
        {
            this.lexicon.setClassifierType(getClassifierType());
            this.lexicon.saveToFile(this.lexiconLocation);
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }

    /**
     * Returns a new or existing Training Corpus backed by a file
     **/
    public FileTrainingCorpus getFileTrainingCorpus() throws IOException
    {
        File raw_file = new File(workdirectory, Parameters.rawName);
        return new FileTrainingCorpus(raw_file);
    }

    /**
     * 由一个目录生成一个训练器
     * Generate an instance of Learner from an existing directory.
     *
     * @param overwrite 是否覆写模型目录下已存在的数据
     *                  deletes any existing data in the model directory
     * @return an instance of a Learner corresponding to the implementationName
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static Learner getLearner(String workdirectory,
                                     String implementationName, boolean overwrite) throws Exception
    {
        File directory = new File(workdirectory);
        if (directory.exists() == false)
            throw new Exception(workdirectory + " must exist");
        if (directory.isDirectory() == false)
            throw new Exception(workdirectory + " must be a directory");

        // create the file names
        String model_file_name = workdirectory + File.separator
                + Parameters.modelName;
        String lexicon_file_name = workdirectory + File.separator
                + Parameters.lexiconName;
        String vector_file_name = workdirectory + File.separator
                + Parameters.vectorName;
        String raw_file_name = workdirectory + File.separator
                + Parameters.rawName;
        Learner learner = null;

        // 删掉已存在的模型 removes existing files for lexicon model and vector
        if (overwrite)
        {
            removeExistingFile(model_file_name);
            removeExistingFile(lexicon_file_name);
            removeExistingFile(vector_file_name);
            removeExistingFile(raw_file_name);
        }

        // 决定使用哪种算法 define which implementation to use
        if (LibSVMModelCreator.equals(implementationName))
            learner = new LibSVMModelCreator(lexicon_file_name,
                                             model_file_name, vector_file_name);
        else if (LibLinearModelCreator.equals(implementationName))
            learner = new LibLinearModelCreator(lexicon_file_name,
                                                model_file_name, vector_file_name);
        else
            throw new Exception(implementationName + " is unknown");

        // reuse the existing lexicon
        if (!overwrite)
        {
            Lexicon oldlexicon = new Lexicon(lexicon_file_name);
            if (oldlexicon != null)
                learner.lexicon = oldlexicon;
        }

        learner.workdirectory = directory;
        return learner;
    }

    /**
     * Returns the parameters passed to the learning engine*
     */
    public String getParameters()
    {
        return parameters;
    }

    /**
     * 设置参数
     * Specifies the parameters passed to the learning engine*
     */
    public void setParameters(String parameters)
    {
        this.parameters = parameters;
    }

    /**
     * 删除单个文件
     *
     * @param path
     */
    private static void removeExistingFile(String path)
    {
        File todelete = new File(path);
        if (todelete.exists())
            todelete.delete();
    }

    public Lexicon getLexicon()
    {
        return lexicon;
    }

}
