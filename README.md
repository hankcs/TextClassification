# 文本分类

这是由 DigitalPebble Ltd 开发的文本分类API，利用libSVM实现了文本分类。
我正在阅读它的代码，并且加入自己的注释。
目标是读完其代码，学习其架构，未来实现更多分类算法。

A Text Classification API in Java originally developed by DigitalPebble Ltd. The API is independent from the ML implementations used and can be used as a front end to various ML algorithms. libSVM and liblinear [https://github.com/bwaldvogel/liblinear-java] are currently embedded.

Standard weighting schemes are provided (occurrences,frequency,tf-idf) for building the vectors. The API aims at facilitating the use of ML for text applications and offers a convenient field-based representation of documents (e.g. title, content, keywords, etc...) with the option to specify a different weighting scheme per field.

A separate project containing examples of use for the API is available on https://github.com/DigitalPebble/textclassification-examples.

It is also used in the GATE TextClassification plugin on https://github.com/DigitalPebble/TextClassificationPlugin. 
