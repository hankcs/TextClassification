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

import java.io.IOException;
import java.util.LinkedList;

/**
 * 存在内存中的语料库
 */
public class RAMTrainingCorpus extends LinkedList<Document> implements TrainingCorpus
{

    private static final long serialVersionUID = 3284220814289135993L;

    public void addDocument(Document doc) throws IOException
    {
        add(doc);
    }

    public void close()
    {
        // nothing to do
    }

}
