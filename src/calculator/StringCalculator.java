package src.calculator;

public class StringCalculator {

	public String separator(String str){
		String ret = ",|:";
		if (str.startsWith("//")){
			int idx = str.indexOf("\n");
			if (idx != 3){
				throw new RuntimeException("커스텀 구문자 오류");
			}
			ret += ("|" + str.charAt(2));
			ret = ret.replace("/\\|$/","\\\\|");
		}
		return ret;
	}

	public int add(String str) {
		if (str.isBlank()){
			return 0;
		}
		String sep = separator(str);
		String[] nums;
		if (str.startsWith("//")){
			nums = str.substring(4).split(sep);
		}
		else{
			nums = str.split(sep);
		}
		int sum = 0;
        for (String s : nums) {
            int num = Integer.parseInt(s);
            if (num < 0)
                throw new RuntimeException("음수 반환");
            sum += Integer.parseInt(s);
        }
		return sum;
	}
}
