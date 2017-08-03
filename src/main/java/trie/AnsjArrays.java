package trie;

public class AnsjArrays {
	private static final int INSERTIONSORT_THRESHOLD = 7;

	/**
	 * 二分法查找.摘抄了jdk的东西..只不过把他的自动装箱功能给去掉了
	 * 
	 * @param branches
	 * @param c
	 * @return
	 */
	public static int binarySearch(WoodInterface[] branches, char c) {
		int high = branches.length - 1;
		if (branches.length < 1) {
			return high;
		}
		int low = 0;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			int cmp = branches[mid].compareTo(c);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

}
