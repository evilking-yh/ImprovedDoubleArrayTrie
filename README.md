# ImprovedDoubleArrayTrie
这是针对大数据集优化了的双数组字典树，使得在大数据集上构建速度也比较满意，查询速度不随数据集的增加而增加，同时解决了数据集需要有序的要求.

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

## 问题

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

## 问题的解决

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


