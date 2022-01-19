import java.io.File;
import java.util.Comparator;

public class Util {
	public static Comparator<File> getFileComparator(){
		return new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				if(o1 == null){
					return o2 == null ? 0 : 1;
				}
				if(o2 == null){
					return -1;
				}
				return o1.getName().compareTo(o2.getName());
			}
		};
	}
}
