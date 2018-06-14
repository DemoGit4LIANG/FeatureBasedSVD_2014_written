# FeatureBasedSVD_2014_written
Java实现的**SVDFeature算法**，这套代码在没有使用多model ensemble的情况下，在2014CCF中国大数据技术创新大赛-多媒体展示广告点击率预估比赛中预测结果排名**全国第7**。

算法出处：

Chen T, Zhang W, Lu Q, et al. SVDFeature: a toolkit for feature-based collaborative filtering[J]. Journal of Machine Learning Research, 2014, 13(1):3619-3622.

源码中同时包含0-1二值版**经典SVD**和**LinearRegression**的实现作为benchmark。

FeatureBasedMatrixFactorization部分由于当初需要拟合竞赛数据，加入了大量缺省值处理函数，因此稍显凌乱。
