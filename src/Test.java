import util.CodeUtil;

/**
 * ============================================================
 * 【临时试验场 Test】—— 验证某个方法到底怎么跑
 * ============================================================
 *
 * 【这个文件该不该留】
 * 严格说，正式项目里不该有这种东西（所以名字才叫 Test，一眼看出是临时的）。
 * 但它有个很实在的用处：**遇到不确定的方法，先在这儿小规模跑一遍**，
 * 而不是直接塞进主程序里试，试坏了还得回滚。
 *
 * 你在写 getCode() 的时候曾经在这儿验证过 System.exit(0) 的行为，
 * 那次做法是对的——那叫"小步验证"。
 *
 * 【本次优化清单】
 *   ① 删掉原来复制过来的一份完整的 getCode() 方法实现
 *      【问题】它是 Login.getCode() 的复制品，两份完全一样的代码。
 *              Login 那边改一行，这边就悄悄过时了——这种"复制出来的第二份"
 *              是 bug 的温床。（前面讲的 DRY 原则）
 *      【优化】现在直接调用 util/CodeUtil.getCode()，全项目只有一份实现。
 *   ② 删掉 int count=1; int wins=0; 这两个没用上的变量
 *   ③ 加一个 main，让这个文件能独立运行
 */
public class Test {

    public static void main(String[] args) {
        // 连跑 10 次验证码，观察两件事：
        //   ① 是不是每次都 6 位
        //   ② 是不是每次都同时含字母和数字
        //   ③ （观察规律）是不是永远只有 1 个数字、5 个字母
        for (int i = 0; i < 10; i++) {
            System.out.println(CodeUtil.getCode());
        }

        // 顺便看看用户ID长什么样
        System.out.println("用户ID示例：" + CodeUtil.createId());

        // 随机数范围测试：验证 randomInt(min,max) 确实含两端
        System.out.println("randomInt(20,40) 的结果：");
        for (int i = 0; i < 10; i++) {
            System.out.print(CodeUtil.randomInt(20, 40) + " ");
        }
        System.out.println();
    }
}
