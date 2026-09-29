package ui;

import domain.User;
import util.CodeUtil;
import util.InputUtil;
import util.ValidateUtil;

import java.util.ArrayList;

/**
 * ============================================================
 * 【登录注册界面 Login】—— 主菜单 + 注册 + 登录
 * ============================================================
 *
 * 【职责】只管"跟用户打交道"：显示菜单、读输入、把结果反馈给用户。
 *        真正的数据存在哪里、怎么找，交给集合和工具类。
 *
 * 【本次优化清单（对照原代码逐条）】
 *
 * ── 结构类 ──
 *   ① 用户集合 userList 从 start() 的局部变量，提升为本类的成员变量
 *      【原代码】ArrayList<User> list=new ArrayList<>(); 写在 start() 里，
 *               然后一路当参数传给 register(list) / login(list)
 *      【问题】  同一个集合在三个方法间传了三趟，方法签名越拖越长。
 *               而且"集合属于谁"这件事说不清楚。
 *      【优化】  改成成员变量——它本来就属于"这次登录会话"这个对象。
 *               数据从"被传来传去"变成"放在它该在的地方"。
 *      【知识点】这跟前面讲的"数据比界面活得久"是同一个道理：
 *               数据要挂在合适的生命周期上。之前挂 start() 是对的
 *               （比挂 register() 强），现在挂到 Login 对象上更自然。
 *
 *   ② 校验方法（checkLen/getCount/checkUsername/checkPassword）
 *      全部搬到 util/ValidateUtil
 *      验证码 getCode() 搬到 util/CodeUtil
 *      → Login 从"什么都干"瘦身成"只管界面"
 *
 *   ③ contains() 删除，只保留 findIndex()
 *      【原代码】contains() 和 findIndex() 两个方法循环体一模一样，
 *               只是一个返回 true 一个返回下标，属于重复实现。
 *               而 login() 里还连着调了两个，等于把集合遍历了两遍：
 *                   if(!contains(list,username)){...}      ← 遍历第1遍
 *                   int index = findIndex(list, username); ← 遍历第2遍
 *      【优化】  只留 findIndex()，判断"存在"就用 findIndex(...) != -1
 *               （前面讲过：重复的知识，改一处要动两地，早晚出事）
 *
 *   ④ Scanner 全部换成 InputUtil（全项目共用一个，不再各自 new）
 *      原代码在 start() 的 while 循环里都 new 了一次 Scanner——
 *      每转一圈就建一个新的，是 6 个 Scanner 里最糟的一个
 *
 * ── 逻辑类 ──
 *   ⑤ register() 改为"数据收集完，最后一步才创建对象"
 *      【原代码】方法一进来就 User u=new User();
 *               然后分两个循环 setName / setPassword
 *      【问题】  ① 中途输入 0 返回时，这个对象白造了（连ID都生成好了）
 *               ② 对象跨越两个循环，读代码时要一直记着"u 现在填到哪了"
 *               ③ 你有参构造 User(name,password) 写出来却从没用过
 *      【优化】  username / password 用局部变量收集，
 *               两个循环都能各自"输入0返回"，最后 new User(username,password)
 *      【注意】 username 和 password 必须**声明在循环外面**，
 *               否则出了循环就销毁了，拿不到——
 *               变量的作用域由它所在的那对大括号决定。
 *
 *   ⑥ 验证码增加次数限制（原来是无限重试）
 *      密码有 3 次机会，验证码却可以无限试，
 *      等于"3 次密码机会"外面套了一层无限循环，次数限制被架空了。
 *      现在改成 3 次，超了返回主菜单。
 *
 *   ⑦ "还有0次机会"的提示语修正
 *      【原代码】第三次输错时先打印"密码错误，还有0次机会"，再提示锁定
 *      【优化】先判断是不是最后一次，再决定说什么话
 *
 * ── 小修 ──
 *   ⑧ 删掉没用到的 import java.util.List;
 *   ⑨ 登录成功后从"游戏结束就 System.exit(0) 杀进程"
 *      改为"正常返回主菜单"（配合 FightGame 的改动）
 *   ⑩ 方法统一加 private（除了 start()，它是给 App 调的）
 *   ⑪ 所有魔法数字（3次机会、3次验证码）抽成常量
 *   ⑫ 修正原第63行缩进错乱
 */
public class Login {

    // ==================== 常量区 ====================

    /** 密码最多输错几次 */
    private static final int MAX_PASSWORD_TRY = 3;

    /** 验证码最多输错几次 */
    private static final int MAX_CODE_TRY = 3;

    // ==================== 字段区 ====================

    /**
     * 用户集合——本次登录会话的所有用户都存在这里。
     *
     * 【注意】这是**内存存储**：程序一关，数据全没了。
     * 真实系统要存数据库（这是后面 JDBC/MySQL 要干的事）。
     * 现在用集合，是为了先把业务逻辑跑通。
     */
    private final ArrayList<User> userList = new ArrayList<>();

    // ==================== 主流程 ====================

    /**
     * 启动登录界面：显示菜单 → 读选择 → 分发到对应功能，循环往复。
     */
    public void start() {
        System.out.println("游戏的登录页面打开了");

        while (true) {
            printMenu();

            String choose = InputUtil.readString();
            switch (choose) {
                case "1":
                    login();
                    break;
                case "2":
                    register();
                    break;
                case "3":
                    System.out.println("用户选择了退出操作");
                    System.exit(0);
                    break;   // 【优化】补上 break 保持格式对称（虽然 exit 之后走不到这里）
                default:
                    System.out.println("输入有误，请重新输入");
            }
        }
    }

    /**
     * 打印主菜单。
     * 【优化】从 start() 里抽出来——方法是"一段有名字的代码"，
     * 名字起好了，主流程就能一眼读懂。
     */
    private void printMenu() {
        System.out.println("┌──────────────────────────────┐");
        System.out.println("│    🎮 欢迎来到文字格斗游戏 🎮 │");
        System.out.println("└──────────────────────────────┘");
        System.out.println("请选择操作：1登录 2注册 3退出");
    }

    // ==================== 注册 ====================

    /**
     * 注册流程：收集用户名 → 收集密码 → 创建用户 → 存进集合。
     *
     * 【注意这两个 while 循环的结构】
     * 每个循环都在做同一件事："反复要输入，直到拿到合法值"。
     * 这类循环必须有一个"出口"，否则用户被锁在里面。
     * 出口有两个：
     *   ① 校验通过 → break 出去，进入下一阶段
     *   ② 用户反悔 → 输入 0 → return 整个方法，回主菜单
     *
     * 【为什么这里必须用 return 而不是 break】
     * break 只跳出**最近的一层**循环。如果这里写 break，
     * 只是跳出了用户名循环，方法还没结束，程序会继续往下走密码循环——
     * 用户还是回不到主菜单。
     * return 是直接结束整个方法，这才是"返回上一级"。
     */
    private void register() {
        System.out.println("用户选择了注册操作");

        // ========== 阶段一：收集用户名 ==========
        String username;
        while (true) {
            System.out.println("请输入用户名,输入0返回上一级");
            username = InputUtil.readString();

            // "输入0返回"必须放在校验之前！
            // 否则 "0" 会先被 checkLen(3,16,"0") 判成"长度不够"，
            // 然后 continue 回去重新要输入——永远轮不到返回逻辑。
            if ("0".equals(username)) {
                System.out.println("已取消注册");
                return;
            }

            // 【优化】长度范围来自常量，判断和下面的提示语共用同一个来源，
            // 将来改需求只动 ValidateUtil 里的常量定义
            if (!ValidateUtil.checkLen(ValidateUtil.USERNAME_MIN, ValidateUtil.USERNAME_MAX, username)) {
                System.out.println("用户名的长度不符合要求，必须是"
                        + ValidateUtil.USERNAME_MIN + "~" + ValidateUtil.USERNAME_MAX + "位之间");
                continue;
            }
            if (!ValidateUtil.checkUsername(username)) {
                System.out.println("用户名的组成不符合要求，必须由字母和数字组成，不能纯数字");
                continue;
            }
            // 【优化】原来是 contains(list,username) 返回 boolean，
            // 现在统一用 findIndex(...) != -1——一个方法解决两种需求
            if (findIndex(username) != -1) {
                System.out.println("用户名已经存在，请重新输入~");
                continue;
            }
            break;   // 校验全过，进入下一阶段
        }

        // ========== 阶段二：收集密码 ==========
        String password;
        while (true) {
            System.out.println("请输入密码,输入0返回上一级");
            String password1 = InputUtil.readString();

            if ("0".equals(password1)) {
                System.out.println("已取消注册");
                return;   // ← 这里也是 return，回主菜单
            }

            if (!ValidateUtil.checkLen(ValidateUtil.PASSWORD_MIN, ValidateUtil.PASSWORD_MAX, password1)) {
                System.out.println("密码不符合要求，必须是"
                        + ValidateUtil.PASSWORD_MIN + "~" + ValidateUtil.PASSWORD_MAX + "位之间");
                continue;
            }
            if (!ValidateUtil.checkPassword(password1)) {
                System.out.println("密码只能是字母加数字的组合，不能有其他字母");
                continue;
            }

            System.out.println("请再次输入密码");
            String password2 = InputUtil.readString();
            if (!password1.equals(password2)) {
                System.out.println("两次密码输入的不一样，请重新输入~");
                continue;
            }

            password = password1;
            break;
        }

        // ========== 阶段三：数据齐了，才创建对象 ==========
        // 【优化】原代码在方法开头就 User u=new User()，两个循环分两次 set 进去。
        // 现在改成"最后一步一次成型"，用上了有参构造。
        // 好处：中途取消不会产生废对象；对象一诞生就是完整可用的。
        User u = new User(username, password);
        userList.add(u);
        System.out.println("注册成功");
    }

    // ==================== 登录 ====================

    /**
     * 登录流程：查用户 → 查状态 → 验密码（带验证码）→ 进游戏。
     */
    private void login() {
        System.out.println("用户选择了登录操作");
        System.out.println("请输入用户名：");
        String username = InputUtil.readString();

        // 【优化】原来 contains() 查一遍、findIndex() 又查一遍，遍历了两次集合。
        // findIndex 返回 -1 就等于"不存在"，一次搞定。
        int index = findIndex(username);
        if (index == -1) {
            System.out.println(username + "未注册，请先注册");
            return;
        }

        // 【优化】拿到 u 之后就一路用 u。
        // 原代码在这儿用了 u，到验密码时又绕回去写 list.get(index)，
        // 两个写法指的是**同一个对象**（list.get 返回的是引用，不是副本），
        // 混着用只会让人读代码时多绕一圈。
        User u = userList.get(index);
        if (!u.isStatus()) {
            System.out.println("用户" + username + "已被禁用，请联系胡绍涵");
            return;
        }

        // ========== 密码验证（最多 MAX_PASSWORD_TRY 次） ==========
        // i 表示"这次错了还剩几次机会"，从 2 递减到 0
        for (int i = MAX_PASSWORD_TRY - 1; i >= 0; i--) {
            System.out.println("请输入密码");
            String password = InputUtil.readString();

            // 【优化】验证码改成有限次尝试（原来 while(true) 是无限次，
            // 把密码的 3 次机会限制架空了）
            if (!checkVerifyCode()) {
                System.out.println("验证码错误次数过多，返回主菜单");
                return;
            }

            if (u.getPassword().equals(password)) {
                System.out.println("密码正确,游戏启动");
                // 【优化】原代码游戏结束后 System.exit(0) 直接杀进程，
                // 现在 gameStart 正常返回，回到这里 → return → 回主菜单。
                new FightGame().gameStart(username);
                return;
            }

            // 【优化】原代码是先打印"还有0次机会"，再判断锁定——
            // 第三次输错时会蹦出"还有0次机会"这种别扭的话。
            // 改成：先判断是不是最后一次，再决定说什么。
            if (i == 0) {
                u.setStatus(false);
                System.out.println("账户已锁定，请联系胡绍涵");
                return;
            }
            System.out.println("密码错误，还有" + i + "次机会");
        }
    }

    /**
     * 验证码校验：最多让用户输 MAX_CODE_TRY 次。
     *
     * 【优化前】原代码是 while(true) 无限循环，验证码错了就一直重来。
     * 【优化后】限定次数，超了就返回 false，由调用方决定怎么处理。
     *
     * 【设计要点】这个方法只回答"验证码过了没有"这一个问题，
     * 至于"没过之后要干嘛"交给调用方——方法职责要单一。
     *
     * @return 验证通过返回 true，超过次数返回 false
     */
    private boolean checkVerifyCode() {
        for (int i = 0; i < MAX_CODE_TRY; i++) {
            String rightCode = CodeUtil.getCode();
            System.out.println("验证码为" + rightCode);
            System.out.println("请输入验证码：");

            String code = InputUtil.readString();
            if (rightCode.equals(code)) {
                System.out.println("验证码输入正确");
                return true;
            }
            System.out.println("验证码错误，请重新输入");
        }
        return false;
    }

    // ==================== 查找 ====================

    /**
     * 在用户集合里按用户名找下标，找不到返回 -1。
     *
     * 【为什么需要"先找下标"】
     * ArrayList 只能按**下标**取元素，不能按内容取。
     * 所以"按用户名找用户"要两步：
     *     ① findIndex() 遍历找出下标
     *     ② get(index) 按下标取出对象
     * 等你学到第6章集合的 HashMap<String,User>，就能一步 map.get(username) 了。
     *
     * 【优化】原代码有 contains() 和 findIndex() 两个方法，
     * 循环体一模一样。现在只保留这个——因为 contains 这个需求
     * 完全可以用 findIndex(username) != -1 表达。
     *
     * @param username 要查找的用户名
     * @return 找到返回下标（≥0），找不到返回 -1
     */
    private int findIndex(String username) {
        for (int i = 0; i < userList.size(); i++) {
            if (username.equals(userList.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }
}
