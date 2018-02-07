# ImprovedDoubleArrayTrie
这是针对大数据集优化了的双数组字典树，使得在大数据集上构建速度也比较满意，查询速度不随数据集的增加而增加，同时解决了数据集需要有序的要求.

关于双数组字典树的具体讲解，可参考 [introduce](https://github.com/evilKing/ImprovedDoubleArrayTrie/blob/master/INTRODUCE.md)

## 说明

这是基于开源实现 https://github.com/komiya-atsushi/darts-java 来修改的双数组字典树的实现.

darts/包下是双数组字典树的实现部分，其中DoubleArrayTrie.java是原始的实现，DoubleLinkedArrayTrie.java是使用数组链表初步优化的实现，DoubleLinkedTrie.java是使用字典树优化后的实现.

trie/包下是字典树的实现部分，(优化后的方案是先构建字典树，然后再转换成双数组字典树)

com.hankcs.test.trie/包下是双数组字典树的测试文件，其中Main.java文件是测darts/DoubleArrayTrie.java文件的，LinkedMain.java文件是测darts/DoubleLinkedArrayTrie.java文件，TrieLinkedMain.java文件是测darts/DoubleLinkedTrie.java文件

_______________________________________________________________________________________


但是在应用到大数据集上时，该实现会有两个问题:

+ 双数组直接使用的是原始数组类型，若数据量特别大，那么这两个数组就需要构造非常非常大，我们知道数组在内存中是使用连续的存储空间，若数据量有十多G，那如何能开辟出连续的十多G内存呢？
```java
	private int check[];
	private int base[];

	private boolean used[];
```

+ Trie树的逻辑实现，该实现是通过依次遍历所有字符串的第一个字符、第二个字符。。。去寻找节点的子节点，这种实现方式在小数据集上没问题，但是在大数据集上这种遍历的过程就特别特别慢了
```java
	private int fetch(Node parent, List<Node> siblings) {
		if (error_ < 0)
			return 0;

		int prev = 0;

		for (int i = parent.left; i < parent.right; i++) {
			if ((length != null ? length[i] : key.get(i).length()) < parent.depth)
				continue;
			String tmp = key.get(i);	//第i行输入字符串
			int cur = 0;	//当前字符的unicode码+1，即下一个非空位
			if ((length != null ? length[i] : tmp.length()) != parent.depth)
				cur = (int) tmp.charAt(parent.depth) + 1;	//按层添加

			if (prev > cur) {
				error_ = -3;
				return 0;
			}

			if (cur != prev || siblings.size() == 0) {
				Node tmp_node = new Node();
				tmp_node.depth = parent.depth + 1;	//父节点的子节点
				tmp_node.code = cur;
				tmp_node.left = i;
				if (siblings.size() != 0)
					siblings.get(siblings.size() - 1).right = i;	//左兄弟节点的右边界更新为当前节点的左边界
				siblings.add(tmp_node);
			}
			prev = cur;
		}

		if (siblings.size() != 0)
			siblings.get(siblings.size() - 1).right = parent.right;

		return siblings.size();
	}
```

## 解决

针对上述两个问题，我提出了两个方法分别去解决这些问题:

+ 使用数组链表去解决第一个问题，数组部分能随机访问，指针的运用可以让数组不用连续存储

+ 事先构造完整的字典树，然后用字典树去构造双数组结构，最后释放掉字典树的内存占用

## 问题 1

优化后，看着逻辑实现没问题啊，单模式匹配结果却不正确，下面是运行TrieLinkedMain.java测试文件的结果，按预期结果应该是能匹配上的.

```
一举
一举一动
一举成名
一举成名天下知
万能
万能胶
是否错误: 0
darts.DoubleLinkedTrie@15db9742
-1
```

希望大神们能帮忙看看，实在无能为力了。。。。。。

### 问题 1 的解决

经过我司一架构师分析，原来是双数组字典树这里需要构建的字典树与普通的稍微有点区别，就是叶子节点不是字符串的最后一个字符，而是在最后一个字符后额外增加了个空的叶子节点
```java
Forest forest = new Forest();
		char[] cs = null;
		for(String line: key){
			cs = line.toCharArray();
			
			WoodInterface tmp = forest;
			for(char c: cs){
				tmp = tmp.add(new Branch(c,false,null));
			}
			tmp.add(new Branch('\0',true,null));    //最后的空叶子节点
		}
		key = null;
```
其他地方都没什么问题了，感谢大神^_^

________________________________________________________________________

## 问题 2

经过上述改进，数据量在二十万条记录左右时，双数组字典树构建完成需要 6 分钟，匹配 1000条记录只需要 4ms；但是当数据量增加到五十万时，我测的构建需要好几个小时，匹配 1000条记录只需要 4ms；这就说明了双数组字典树的其中一个优点，匹配速度只与字符串本身的长度有关，与数据量无关。

但是数据量增加到五十万时的构建速度是无法接受的，这就需要进一步优化了.

### 问题 2 的分析

进一步查看了构建双数组部分的代码，主要包括三个阶段：

- 查找非冲突点
- 更新 check数组的值
- 更新 base数组的值

我们的check数组和base数组都是通过数组链表来实现的，其中数组的 *BUFF_SIZE* 的大小与链表指针的查询速度有关，所以这里可以把 *BUFF_SIZE* 设置大一点，这样指针链接的次数就少一些，可提高查询速度.

另外查找非冲突点这里，由于数据量比较大，所以可能会有前面的数组值都已经铺满了，这样就会经常有冲突的情况，导致了很多循环遍历。
我们可以设置当前面的冲突率超过一定阈值的时候，就更新基础检查点；同时可以动态的扩大每次 *pos* 索引的增长步长.

通过这几点的改善，应该可以提高构建速度.

### 问题 2 的解决

实现上述改进方案后，BUFF_SIZE设置成了 100000 ，pos 根据动态生成随机数来动态取 pos + 1 还是 pos + 2，去增加步长，修改适当的冲突率.

运行 一百万条记录去构建，需要三分钟，匹配一万条记录，需要 0.58秒。

实时证明该方案非常有效.


## 问题 3

> 看到有几个关注了本项目，续写一下。。。

进过上述改进，在百万级数据量时可以很好的运行，但当数据量增加到千万级或者上亿级别时，速度又会慢下了。

### 问题 3 的分析

原因是上面使用的底层数据结构是 **数组链表**，当数据量达到上千万时，程序运行到后面每次都要从头开始索引很长的数组快才能找到合适的位置，这个过程是很好时间的，所以我们得想办法优化这个底层数据结构。

### 问题 4 的解决

我参考 HashMap 的实现原理，由于这里只是使用索引来判断，而且对数据结构的操作只有增加和修改，所以我设计了索引数组链表的数据结构。

采用二级索引的方式，第一层索引是一个数组，每个元素持有第二层若干块的第一块的应用，第二层是一个数组链表，这样要查一个新的元素，就可以首先根据第一层索引快速找到元素所在的区域，然后根据持有的引用在第二层小区域内快速找到正确的元素。

经过改进，构建七千万数据集也只需要二十多分钟。

这个数据结构可以独立出来用在其他大数据场合，所以我单独创建了个工程，读者需要自行替换本项目中的数组链表结构，参考 [IndexArrayList](https://github.com/evilKing/IndexArrayList)






