import java.io.File;
import java.math.BigDecimal;
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
				if(Character.isDigit(o1.getName().charAt(0))){
					return compareFromNumbers(o1.getName(), o2.getName());
				}
				return o1.getName().compareTo(o2.getName());
			}
		};
	}

	protected static int compareFromNumbers(String name1, String name2) {
		String number1 = "", number2 = "";
		for(char c : name1.toCharArray()){
			if(Character.isDigit(c)){
				number1 += c;
			}else{
				break;
			}
		}
		for(char c : name2.toCharArray()){
			if(Character.isDigit(c)){
				number2 += c;
			}else{
				break;
			}
		}
		if(number2.isEmpty()){
			return -1;
		}
		return new BigDecimal(number1).compareTo(new BigDecimal(number2));
	}
}
