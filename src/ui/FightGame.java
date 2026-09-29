package ui;

import domain.EnemyCharacter;
import domain.HeroCharacter;
import util.CodeUtil;
import util.InputUtil;

import java.util.ArrayList;

/**
 * ============================================================
 * 【战斗界面 FightGame】—— 游戏主流程
 * ============================================================
 *
 * 【本次优化清单（对照原代码逐条）】
 *
 * ── 🔴 逻辑错误（会实实在在影响游戏） ──
 *
 *   ① 【严重BUG】"快速攻击"和"火球术"两个技能**只喊不打**
 *      【原代码】
 *          case "快速攻击":
 *              int damage3 = 0;
 *              for (...) { damage3 += temp; }
 *              System.out.println("...造成了" + damage3 + "点伤害！");
 *              break;          ← 算完了伤害，但从来没调用 player.takeDamage()！
 *      问题：伤害算出来了、也打印出来了，但**玩家的血一点没掉**。
 *            敌人喊了一嗓子就走了。
 *      同样的错误在 case "火球术" 里一模一样地犯了一遍。
 *      修复：补上 player.takeDamage(伤害)。
 *
 *      【这是怎么造成的】典型的"复制粘贴漏一行"——写第一段时记得扣血，
 *      后面几段照着抄，抄着抄着就把那行丢了。
 *      和之前"密码循环漏 break""switch 的 case 漏 break"是同一类病：
 *      **结构化复制时，对称的位置漏了东西**。
 *      防它的办法：写完一组相似的 case 后，横向对比一遍它们各自都调了什么。
 *
 *   ② 【BUG】场次编号会跳号
 *      【原代码】count 变量在三处被 ++：
 *          击败敌人时  count++;    ← 这里加了
 *          战后选 y 时 count++;    ← 这里又加了一次
 *          default 时  count++;
 *      结果：第1场打赢后 count 从 1 → 2（击败时）→ 3（选y时），
 *            第二场开局显示的是"第3场战斗开始"。跳号了。
 *      修复：**根本不需要 count**。
 *          打过的场次 = wins（赢了几场就打了几场），
 *          当前场次 = wins + 1。用已有的数据推导，不额外记一份。
 *      【知识点】两个变量记录同一件事 = 早晚对不上。
 *                能推导出来的值就别单独存。
 *
 * ── 🟡 设计问题 ──
 *
 *   ③ createPlayerCharacter() 里的**匿名内部类**是多余的
 *      【原代码】
 *          HeroCharacter player = new HeroCharacter(username, ..., defPoint) {
 *
 *          };
 *      那个空的大括号 { } 让这句变成了"匿名内部类"（第11章学过）。
 *      但大括号里什么都没写，纯属多余。
 *      匿名内部类的意义是"顺便覆盖几个方法"，空的时候就是白写。
 *      修复：直接 new HeroCharacter(...)，去掉 { }。
 *
 *   ④ getHealthBar() 里定义了常量却没用
 *      【原代码】
 *          int barLength = 20;                          ← 定义了
 *          int filled = (int) (HP * 1.0 * 20 / maxHP);  ← 用字面量 20
 *          for (int j = 0; j < 20 - filled; j++) {      ← 又是字面量 20
 *      这就是我们前面讲过的"两处硬编码"：定义了 barLength 却不用它，
 *      等于告诉自己"这个数是配置"，然后又到处写死。
 *      修复：全部改用 BAR_LENGTH，并提到类常量区。
 *      另外加了"防越界"保护，避免血量异常时血条画到框外。
 *
 *   ⑤ playerTurn() 的 switch 用了"故意穿透"
 *      【原代码】
 *          switch (choose) {
 *              default:
 *                  System.out.println("没有这个操作，默认使用普通攻击");
 *              case "1":              ← default 故意"掉进" case "1"
 *                  ...
 *      这是"穿透"的合法用法（default 不写 break，让它落到普通攻击），
 *      但非常危险：将来谁把 case 顺序调一下，整个逻辑就废了。
 *      修复：先把非法输入**规范化**成 "1"，再用正常的 switch。
 *      【知识点】穿透是 switch 的特性不是 bug，但除非多值共用同一段代码，
 *                否则一律显式写 break。
 *
 *   ⑥ playerTurn() case "3" 里多余的判断
 *      【原代码】
 *          player.heal(healHP);
 *          if (player.HP >= player.maxHP) {
 *              player.HP = player.maxHP;
 *          }
 *      heal() 内部**已经**做了"不超过上限"的保护（见 GameCharacter），
 *      外面再判一次是重复。删掉。
 *
 * ── 🟡 规范问题 ──
 *
 *   ⑦ 全部直接访问对象字段的写法改成 getter/setter
 *      原代码里到处是 c.maxHP += 10、player.attack、enemy.name 这种，
 *      因为那时字段是 public。现在字段都改成 private 了，
 *      必须走方法：c.setMaxHp(c.getMaxHp() + 10)。
 *      【为什么要绕这一圈】因为方法里能加规则。比如 setHp() 保证血量
 *      永远在 [0, maxHp]，外部怎么乱传都不会把游戏搞坏。
 *
 *   ⑧ r.nextInt(20, 41) 这种写法 **JDK 8 不支持**
 *      ⚠️ 蓝桥杯竞赛环境是 Eclipse + JDK 1.8，那里没有两个参数的 nextInt。
 *      统一改用 CodeUtil.randomInt(min, max)（内部用 JDK8 兼容写法）。
 *
 *   ⑨ Random 反复创建 → 改用 CodeUtil 里唯一的那个
 *      原代码在三个方法里各 new 了一次 Random。
 *
 *   ⑩ Scanner 反复创建 → 改用 InputUtil 共用的那一个
 *
 *   ⑪ 大方法拆小
 *      gameStart() 原本一百多行，把五件事混在一起。
 *      拆出：printWelcome / createEnemyList / strengthenEnemies /
 *            pickEnemy / fight / afterBattle / askContinue
 *      判断标准：**一个方法只做一件事，方法名能读出这件事**。
 *
 *   ⑫ 除 gameStart() 外，其余方法全部加 private
 *      它们只服务于本类内部，暴露成 public 没有意义。
 *
 *   ⑬ 类里的数字抽成常量（血条长度、基础属性、升级门槛…）
 */
public class FightGame {

    // ==================== 常量区 ====================

    /** 玩家可选技能。放这里是为了让"显示菜单"和"存进角色"用同一份数据 */
    private static final String[] SKILLS = {"普通攻击", "强力一击", "生命汲取"};

    /** 血条总格数 */
    private static final int BAR_LENGTH = 20;

    /** 创建角色可分配的属性点总数 */
    private static final int TOTAL_POINTS = 50;

    /** 角色基础属性（不分配任何点数时的值） */
    private static final int BASE_HP = 100;
    private static final int BASE_ATTACK = 10;

    /** 每分配1点对应的加成 */
    private static final int HP_PER_POINT = 10;
    private static final int ATTACK_PER_POINT = 2;

    /** 每赢几场升一次属性 */
    private static final int WIN_PER_LEVEL_UP = 3;

    /** 使用主动技能的代价（消耗生命值） */
    private static final int SKILL_COST = 10;

    /** 每场胜利后敌方全体强化的幅度 */
    private static final int ENEMY_HP_UP = 10;
    private static final int ENEMY_ATTACK_UP = 3;
    private static final int ENEMY_DEFENSE_UP = 2;

    // ==================== 主流程 ====================

    /**
     * 游戏主入口。
     *
     * 【优化说明】原方法一百多行，现在只保留"主干"：
     * 创建角色 → 循环{强化敌人 → 挑对手 → 打一场 → 战后处理 → 问是否继续}
     * 每一步的具体实现都进了自己的方法，读这段就像读目录。
     *
     * 【另一个改动】原代码结尾是 System.exit(0)（直接杀掉整个程序），
     * 现在改成正常返回。这样玩家退出游戏后会回到登录主菜单，
     * 而不是必须重启程序。
     */
    public void gameStart(String username) {
        printWelcome(username);

        HeroCharacter player = createPlayerCharacter(username);
        System.out.println("角色创建成功！");
        System.out.print("*初期属性为：");
        player.show();
        System.out.print("*拥有的技能：");
        System.out.println(String.join(",", SKILLS));

        ArrayList<EnemyCharacter> enemyList = createEnemyList();
        int wins = 0;

        // 只要玩家还活着，就一直打下去
        while (player.isAlive()) {
            // 第一场用初始敌人，从第二场起敌人变强
            // 【说明】每场开始前把所有敌人拉满血并强化——
            // 这样每次随机碰到的都是满状态对手，不会出现残血敌人白送。
            if (wins != 0) {
                strengthenEnemies(enemyList);
            }

            EnemyCharacter enemy = pickEnemy(enemyList);
            enemy.show();

            System.out.println("──────────────");
            // 【优化】原代码用 count 记场次，会跳号（1→3→5）。
            // 用 wins+1 推导：赢了几场，现在就是第几+1场。
            System.out.println("第" + (wins + 1) + "场战斗开始！对手：" + enemy.getName());

            // 打一场；玩家阵亡就结束整个游戏
            if (!fight(player, enemy)) {
                break;
            }

            wins++;
            afterBattle(player, wins);

            if (!askContinue()) {
                break;
            }
        }

        printGameOver(wins);
    }

    /** 开场横幅 */
    private void printWelcome(String username) {
        System.out.println("┌──────────────────────────────────────────┐");
        System.out.println("│    🎮 欢迎" + username + "来到文字格斗游戏 🎮 │");
        System.out.println("└──────────────────────────────────────────┘");
    }

    /** 结算画面 */
    private void printGameOver(int wins) {
        System.out.println("────────────────────────");
        System.out.println("游戏结束");
        System.out.println("总胜场" + wins);
        System.out.println("感谢你玩我的游戏");
    }

    // ==================== 角色创建 ====================

    /**
     * 让玩家分配属性点，创建一个玩家角色。
     */
    private HeroCharacter createPlayerCharacter(String username) {
        System.out.println("创建您的角色：");
        System.out.println("您的角色名为：" + username);

        int hpPoint;
        int atkPoint;
        int defPoint;

        while (true) {
            System.out.println("请分配属性点(共" + TOTAL_POINTS + "点)");
            System.out.println("生命值(每点+" + HP_PER_POINT + " HP)");
            System.out.println("攻击力(每点+" + ATTACK_PER_POINT + " ATK)");
            System.out.println("防御力(每点+1 DEF)");

            // 【优化】改用 InputUtil.readInt()——用户输字母时提示重输，
            // 而不是像原来 sc.nextInt() 那样直接抛异常让程序崩掉
            hpPoint = InputUtil.readInt();
            atkPoint = InputUtil.readInt();
            defPoint = InputUtil.readInt();

            if (hpPoint < 0 || atkPoint < 0 || defPoint < 0) {
                System.out.println("无效输入，不能为负数，请重新分配");
                continue;
            }
            // 【优化】原代码这里还有一段
            //     if (hpPoint > points || atkPoint > points || defPoint > points)
            // 是多余的：三项之和都不超过总数，那每一项当然也不超过。
            // 上一个 if 已经把它覆盖了，删掉。
            if (hpPoint + atkPoint + defPoint > TOTAL_POINTS) {
                System.out.println("分配的点数超过了总数" + TOTAL_POINTS + "，请重新分配");
                continue;
            }
            break;
        }

        // 【优化】原代码这里写成了匿名内部类：
        //     new HeroCharacter(...) { };
        // 那对空的大括号让它变成了匿名内部类，但里面什么都没写。
        // 匿名内部类的意义是"顺便覆盖几个方法"，空的纯属多余，去掉。
        HeroCharacter player = new HeroCharacter(
                username,
                BASE_HP + hpPoint * HP_PER_POINT,
                BASE_ATTACK + atkPoint * ATTACK_PER_POINT,
                defPoint);

        // 【优化】技能列表从常量 SKILLS 来，不再手写三遍字符串
        for (String skill : SKILLS) {
            player.getSkillList().add(skill);
        }

        return player;
    }

    // ==================== 敌人相关 ====================

    /**
     * 创建敌人列表。
     * 【说明】这里的数值是"关卡配置"，写在这里方便调平衡；
     * 如果以后敌人多了，可以挪到配置文件或数据库。
     */
    private ArrayList<EnemyCharacter> createEnemyList() {
        ArrayList<EnemyCharacter> enemyList = new ArrayList<>();
        enemyList.add(new EnemyCharacter("初级战士", 80, 15, 10, "猛击"));
        enemyList.add(new EnemyCharacter("敏捷刺客", 60, 20, 5, "快速攻击"));
        enemyList.add(new EnemyCharacter("重装坦克", 120, 10, 20, "防御姿态"));
        enemyList.add(new EnemyCharacter("神秘法师", 70, 25, 8, "火球术"));
        return enemyList;
    }

    /**
     * 所有敌人变强并把血拉满。
     *
     * 【优化】原代码用普通 for + get(i) 遍历：
     *     for (int i = 0; i < enemyList.size(); i++) {
     *         EnemyCharacter c = enemyList.get(i);
     *         ...
     *     }
     * 这里不需要下标，只需要每个元素 → 用增强 for 更直接。
     * 【判断标准】遍历时用得到下标（比如要 i-- 或按下标取别的数组）就用普通 for，
     *            用不到就用增强 for。
     */
    private void strengthenEnemies(ArrayList<EnemyCharacter> enemyList) {
        for (EnemyCharacter c : enemyList) {
            c.setMaxHp(c.getMaxHp() + ENEMY_HP_UP);
            c.setHp(c.getMaxHp());          // 拉满血
            c.setAttack(c.getAttack() + ENEMY_ATTACK_UP);
            c.setDefense(c.getDefense() + ENEMY_DEFENSE_UP);
            c.setDefending(false);          // 清掉上一场残留的防御姿态
        }
    }

    /** 随机挑一个敌人当对手 */
    private EnemyCharacter pickEnemy(ArrayList<EnemyCharacter> enemyList) {
        int index = CodeUtil.randomInt(0, enemyList.size() - 1);
        return enemyList.get(index);
    }

    // ==================== 一场战斗 ====================

    /**
     * 打一场：玩家和敌人轮流行动，直到一方倒下。
     *
     * 【优化】从 gameStart 里抽出来的。原来这段代码嵌在两层 while 中间，
     * 读的时候要先分清哪层是哪层。
     *
     * @return 玩家赢了返回 true，玩家阵亡返回 false
     */
    private boolean fight(HeroCharacter player, EnemyCharacter enemy) {
        int round = 1;

        while (player.isAlive()) {
            System.out.println("──────────────");
            System.out.println("第" + round + "个回合开始！对手：" + enemy.getName());
            System.out.println(getHealthBar(player.getName(), player.getHp(), player.getMaxHp()));
            System.out.println(getHealthBar(enemy.getName(), enemy.getHp(), enemy.getMaxHp()));

            // 玩家先手
            playerTurn(player, enemy);
            if (!enemy.isAlive()) {
                System.out.println("🎉 你击败了" + enemy.getName() + "!");
                break;
            }

            // 敌人后手
            enemyTurn(enemy, player);
            if (!player.isAlive()) {
                System.out.println("💀你被" + enemy.getName() + "击败了....");
                break;
            }

            round++;
        }

        return player.isAlive();
    }

    /**
     * 战斗结束后：回血、检查升级。
     */
    private void afterBattle(HeroCharacter player, int wins) {
        int healHp = CodeUtil.randomInt(20, 40);
        player.heal(healHp);
        System.out.println("❤ 战斗结束！ 你恢复了" + healHp + "点生命值");
        System.out.println("🏆 当前胜场" + wins);
        System.out.println("──────────────────────");

        if (wins % WIN_PER_LEVEL_UP == 0) {
            System.out.println("⭐恭喜!你获得了属性提升");
            int addHp = CodeUtil.randomInt(20, 50);
            int addAtk = CodeUtil.randomInt(5, 10);
            int addDef = CodeUtil.randomInt(3, 10);

            player.setMaxHp(player.getMaxHp() + addHp);
            player.setAttack(player.getAttack() + addAtk);
            player.setDefense(player.getDefense() + addDef);

            System.out.println("最大生命值" + addHp + "  攻击力" + addAtk + "  防御力" + addDef);
            System.out.print("当前属性:");
            player.show();
        }
    }

    /**
     * 问玩家要不要继续下一场。
     *
     * 【优化】原代码是三分支 if-else，y/n/其他各写一段。
     * 现在是"先判断 n（唯一的否），剩下的都算继续"，
     * 非法输入提示完直接继续——分支少了一半。
     *
     * @return true 继续游戏，false 退出
     */
    private boolean askContinue() {
        System.out.println("继续下一场战斗?(y/n)");
        String choose = InputUtil.readString();

        if (choose.equalsIgnoreCase("n")) {
            return false;
        }
        if (!choose.equalsIgnoreCase("y")) {
            System.out.println("没有这个选择，默认游戏继续");
        }
        return true;
    }

    // ==================== 玩家的回合 ====================

    /**
     * 玩家选择并执行行动。
     */
    private void playerTurn(HeroCharacter player, EnemyCharacter enemy) {
        System.out.println("====你的回合====");

        // 【优化】菜单从 SKILLS 常量生成，不再手写三行 println。
        // 将来加技能只要改 SKILLS 一个地方（注意：加技能同时要在下面的
        // switch 里补一个 case，这是目前的小局限）
        for (int i = 0; i < SKILLS.length; i++) {
            System.out.println((i + 1) + "." + SKILLS[i]);
        }
        System.out.println("选择行动(1-" + SKILLS.length + ")");

        String choose = InputUtil.readString();

        // 【优化】原代码用的是"故意穿透"：
        //     default:
        //         System.out.println("没有这个操作，默认使用普通攻击");
        //     case "1":       ← default 不写 break，直接掉进 case "1"
        // 这写法合法但危险，以后谁调整了 case 顺序就会出错。
        // 现在改成先把非法输入"规范化"成 "1"，switch 就干净了。
        if (!"1".equals(choose) && !"2".equals(choose) && !"3".equals(choose)) {
            System.out.println("没有这个操作，默认使用普通攻击");
            choose = "1";
        }

        switch (choose) {
            case "1": {
                int damage = calculateDamage(player.getAttack(), enemy.getDefense());
                System.out.println("⚔️ 你对 " + enemy.getName() + " 使用了普通一击，造成了" + damage + "点伤害！");
                enemy.takeDamage(damage);
                break;
            }
            case "2": {
                if (player.getHp() > SKILL_COST) {
                    player.takeDamage(SKILL_COST);
                    int damage = calculateDamage((int) (player.getAttack() * 1.8), enemy.getDefense());
                    System.out.println("💥 消耗" + SKILL_COST + "点HP，你对 " + enemy.getName()
                            + "使用了强力一击，造成了" + damage + "点伤害");
                    enemy.takeDamage(damage);
                } else {
                    System.out.println("体力不足，攻击失败");
                }
                break;
            }
            case "3": {
                if (player.getHp() > SKILL_COST) {
                    player.takeDamage(SKILL_COST);
                    int healHp = CodeUtil.randomInt(0, 20);
                    player.heal(healHp);
                    // 【优化】原代码在这之后还写了一段
                    //     if (player.HP >= player.maxHP) { player.HP = player.maxHP; }
                    // 但 heal() 内部已经保证不会超过上限了，这里纯属重复，删掉。
                    System.out.println("💚 消耗" + SKILL_COST + "点生命值，你使用了生命汲取技能，恢复了"
                            + healHp + "点生命值");
                } else {
                    System.out.println("体力不足，恢复生命失败");
                }
                break;
            }
            default:
                // 上面已经做过规范化，理论上走不到这里
                break;
        }
    }

    // ==================== 敌人的回合 ====================

    /**
     * 敌人行动：50% 概率放技能，否则普通攻击。
     */
    private void enemyTurn(EnemyCharacter enemy, HeroCharacter player) {
        System.out.println("====" + enemy.getName() + "的回合====");

        // 10 以内随机一个数，>=5 就用技能 → 相当于 50% 概率
        String action = "普通攻击";
        if (CodeUtil.randomInt(0, 9) >= 5) {
            action = enemy.getSkill();
        }

        switch (action) {
            case "普通攻击": {
                System.out.println("敌人采取了普通攻击");
                int damage = calculateDamage(enemy.getAttack(), player.getDefense());
                System.out.println("⚔️ " + enemy.getName() + "对 " + player.getName()
                        + " 使用了普通一击，造成了" + damage + "点伤害！");
                player.takeDamage(damage);
                break;
            }
            case "猛击": {
                System.out.println("当前的战士采取了猛击");
                int damage = calculateDamage((int) (enemy.getAttack() * 1.5), player.getDefense());
                System.out.println("⚔️ " + enemy.getName() + "对 " + player.getName()
                        + " 使用了猛击，造成了" + damage + "点伤害！");
                player.takeDamage(damage);
                break;
            }
            case "快速攻击": {
                System.out.println("当前的刺客采取了快速攻击");
                // 连续攻击两次，每次伤害减半
                int totalDamage = 0;
                for (int i = 0; i < 2; i++) {
                    totalDamage += calculateDamage((int) (enemy.getAttack() * 0.5), player.getDefense());
                }
                System.out.println("⚔️ " + enemy.getName() + "对 " + player.getName()
                        + " 使用了快速攻击，造成了" + totalDamage + "点伤害！");
                // 🔴【本次修复的BUG】原代码到此就 break 了——
                // 伤害算出来、字也打印了，但玩家一滴血没掉。
                // 补上这一行，技能才真正生效。
                player.takeDamage(totalDamage);
                break;
            }
            case "防御姿态": {
                System.out.println("当前的坦克采取了防御姿态");
                enemy.setDefending(true);
                System.out.println("🛡" + enemy.getName() + "摆出了防御姿态");
                break;
            }
            case "火球术": {
                System.out.println("当前的法师采取了火球术");
                int damage = calculateDamage((int) (enemy.getAttack() * 1.8), player.getDefense());
                System.out.println("🔥" + enemy.getName() + "对你造成了" + damage + "点伤害");
                // 🔴【本次修复的BUG】同上——原代码缺这一行，火球术也是只喊不打。
                player.takeDamage(damage);
                break;
            }
            default:
                break;
        }
    }

    // ==================== 显示与计算 ====================

    /**
     * 画一条血条，形如：张三[████████            ]80/120 HP
     *
     * 【优化点明细】
     *   ① 【原代码】int barLength = 20; 定义完就再没用过，
     *      后面两处都直接写死 20。现在全部换成常量 BAR_LENGTH。
     *   ② 【原代码】filled = (int)(HP * 1.0 * 20 / maxHP)，
     *      先转 double 再除，是为了避免整数除法把小数部分砍掉。
     *      但其实整数运算 (hp * BAR_LENGTH / maxHp) 结果完全一样
     *      （正数情况下，整数除法同样是向下取整），而且更简洁。
     *   ③ 【新增】filled 的上下限保护：万一血量数据异常，
     *      血条也不会画出框外或画出负数个方块。
     */
    private String getHealthBar(String name, int hp, int maxHp) {
        int filled = hp * BAR_LENGTH / maxHp;
        if (filled < 0) {
            filled = 0;
        }
        if (filled > BAR_LENGTH) {
            filled = BAR_LENGTH;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(name).append("[");

        for (int i = 0; i < filled; i++) {
            sb.append("█");
        }
        for (int i = 0; i < BAR_LENGTH - filled; i++) {
            sb.append(" ");
        }

        sb.append("]");
        sb.append(hp).append("/").append(maxHp).append(" HP");
        return sb.toString();
    }

    /**
     * 计算实际伤害 = 攻击 - 防御，最低 1 点。
     *
     * 【为什么要保底1点】否则高防御角色会完全免疫，
     * 战斗永远打不完（双方都打不动对方）。
     *
     * 【优化】原代码用 if 判断后 return，现在用三元表达式一行写完。
     */
    private int calculateDamage(int attack, int defense) {
        int damage = attack - defense;
        return damage < 1 ? 1 : damage;
    }
}
