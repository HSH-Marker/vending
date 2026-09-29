package util;

/**
 * ============================================================
 * 【校验工具类 ValidateUtil】—— 字符串的长度与组成规则
 * ============================================================
 *
 * 【为什么要有这个类】
 * 原代码把 checkLen / getCount / checkUsername / checkPassword
 * 四个方法全塞在 Login 里，导致 Login 又管菜单、又管注册登录、
 * 又管字符串校验——一个类干了三件事。
 * 校验规则和"登录界面"没有半点关系，谁都能用，所以抽出来。
 *
 * 【优化清单】
 *   ① 四个校验方法从 Login 移到本类
 *   ② 【重点】3~16、3~8 这些数字抽成常量
 *
 * 【重点说说②：为什么要抽常量】
 * 原代码里"用户名的长度范围"写了两遍：
 *     Login.java 第45行：if(!checkLen(3,16,username))       ← 传参时写一遍
 *     Login.java 第46行：println("...必须是3~16位之间")        ← 提示语里又写一遍
 * 将来需求改成"5~12位"，你要同时改这两个地方，
 * 漏掉一个就变成"程序校验5~12位，却提示用户必须3~16位"。
 *
 * 抽成常量后，判断和提示语都引用同一个常量：
 *     checkLen(USERNAME_MIN, USERNAME_MAX, username)
 *     println("必须是" + USERNAME_MIN + "~" + USERNAME_MAX + "位")
 * 改需求时只动常量定义那一行，两处自动跟着变。
 *
 * 这叫"单一数据源"——同一个知识只在一个地方描述。
 */
public class ValidateUtil {

    // ==================== 常量区（改规则只动这里） ====================

    /** 用户名长度下限 */
    public static final int USERNAME_MIN = 3;
    /** 用户名长度上限 */
    public static final int USERNAME_MAX = 16;

    /** 密码长度下限 */
    public static final int PASSWORD_MIN = 3;
    /** 密码长度上限 */
    public static final int PASSWORD_MAX = 8;

    /** 工具类不允许被 new */
    private ValidateUtil() {
    }

    // ==================== 长度校验 ====================

    /**
     * 判断字符串长度是否在 [minLen, maxLen] 之间（含两端）。
     *
     * 【原代码】
     *     //判断长度
     *     public boolean checkLen(int minLen ,int maxLen,String str){
     *         if(str.length()>minLen&&str.length()<maxLen) {
     *             return true;
     *         }else {
     *             return false;
     *         }
     *     }
     * 你把这段注释掉、改成一行 return，改得对——
     * "条件本身就是一个 boolean"，不需要再套 if 返回 true/false。
     * （但注意原版用的是 > 和 <，是"不含两端"；实际需求是含两端，所以用 >= <=）
     *
     * @param minLen 最小长度（含）
     * @param maxLen 最大长度（含）
     * @param str    待检查的字符串
     */
    public static boolean checkLen(int minLen, int maxLen, String str) {
        return str.length() >= minLen && str.length() <= maxLen;
    }

    // ==================== 字符组成统计 ====================

    /**
     * 统计字符串里"字母 / 数字 / 其他字符"各有多少个。
     *
     * 【设计亮点（保留原思路）】一次遍历同时数三类，返回 int[3]。
     * 没有写成"数一遍字母、再数一遍数字、再数一遍其他"三遍循环。
     * 数组含义：arr[0]=字母数，arr[1]=数字数，arr[2]=其他字符数
     *
     * 【优化点】这个方法被 checkUsername 和 checkPassword 复用，
     * 两者只改最后的判断条件——这就是"提取公共逻辑"。
     *
     * @return int[3]：{字母个数, 数字个数, 其他字符个数}
     */
    public static int[] getCount(String str) {
        int charCount = 0;
        int numCount = 0;
        int otherCount = 0;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isLetter(c)) {
                charCount++;
            } else if (Character.isDigit(c)) {
                numCount++;
            } else {
                otherCount++;
            }
        }

        return new int[]{charCount, numCount, otherCount};
    }

    // ==================== 组成规则 ====================

    /**
     * 用户名规则：必须有字母，且不能出现字母数字以外的字符。
     * （即：不能纯数字，也不能带特殊符号）
     *
     * 判断依据：arr[0]>0 → 至少有1个字母；arr[2]==0 → 没有非法字符
     */
    public static boolean checkUsername(String username) {
        int[] arr = getCount(username);
        return arr[0] > 0 && arr[2] == 0;
    }

    /**
     * 密码规则：字母和数字都必须有，且不能有字母数字以外的字符。
     *
     * 判断依据：arr[0]>0 有字母 + arr[1]>0 有数字 + arr[2]==0 无非法字符
     */
    public static boolean checkPassword(String password) {
        int[] arr = getCount(password);
        return arr[0] > 0 && arr[1] > 0 && arr[2] == 0;
    }
}
