package domain;

/**
 * ============================================================
 * 【敌人角色 EnemyCharacter】—— 继承自 GameCharacter
 * ============================================================
 *
 * 【它是本项目"多态"的最佳例子】
 *   父类 GameCharacter 说"受伤就是扣血"，所有角色默认都这么做。
 *   但敌人有"防御姿态"这个特殊能力——开了防御，受到的伤害减半。
 *   于是 EnemyCharacter **重写**（@Override）了 takeDamage()。
 *
 * 【多态的关键】战斗代码里调用的是 player.takeDamage(...)，
 * 但 player 身上实际挂着哪个对象，就执行哪个版本的 takeDamage()。
 * 调用方完全不用写 if (这是敌人) {...} else {...} 来判断——
 * 这就是"编译看左边，运行看右边"。
 *
 * 【本次优化清单】
 *   ① skill / defending 改成 private + getter/setter
 *   ② 防御减伤那行三元表达式拆开写——原代码
 *      damage = damage / 2 > 1 ? damage / 2 : 1;
 *      把 damage/2 算了两遍，读起来也要绕一下，改成 if 更直白
 *   ③ 删除没用到的无参构造里的冗余代码（保留无参构造本身，见下方说明）
 */
public class EnemyCharacter extends GameCharacter {

    /** 这个敌人的技能名（"猛击"/"快速攻击"/"防御姿态"/"火球术"） */
    private String skill;

    /**
     * 是否处于防御姿态。
     * true 时受到的下一次伤害减半，挨完这一次自动解除。
     *
     * 【原代码】public boolean defending;
     * 【优化】private + isDefending()/setDefending()
     */
    private boolean defending;

    /**
     * 无参构造。
     * 【说明】保留它是因为子类构造需要显式或隐式调用 super()，
     * 且将来可能有"先造空对象再填数据"的用法。
     */
    public EnemyCharacter() {
        super();
    }

    /**
     * 全参构造：前四个属性交给父类，技能名是自己特有的。
     */
    public EnemyCharacter(String name, int hp, int attack, int defense, String skill) {
        super(name, hp, attack, defense);
        this.skill = skill;
    }

    /**
     * 【重写】受到伤害——防御姿态下伤害减半（最低1点）。
     *
     * 【为什么要减半后还要"最低1点"】
     * 如果攻击力远低于防御力，算出来的伤害可能是 0 甚至负数，
     * 那玩家就会看到"打了半天对面一滴血不掉"。
     * 所以保底 1 点，保证战斗一定能推进。
     *
     * 【原代码】
     *     @Override
     *     public void takeDamage(int damage) {
     *         if(defending){
     *             damage = damage / 2 > 1 ? damage / 2 : 1;
     *             defending=false;
     *         }
     *         super.takeDamage(damage);
     *     }
     *
     * 【优化点】
     *   damage / 2 > 1 ? damage / 2 : 1 里 damage/2 算了两遍，
     *   而且嵌套在一个表达式里不好读。拆成 if 之后：
     *   "先减半，再保底"，两句话两件事。
     *
     * 【注意 super.takeDamage(damage) 这句】
     *   重写不等于替换。父类那套"扣血 + 血量不低于0"的逻辑仍然要用，
     *   用 super 调父类版本，在它之前先把自己的特殊规则处理完。
     *   这是重写里最常见的写法：**先算，再交给父类执行**。
     */
    @Override
    public void takeDamage(int damage) {
        if (defending) {
            damage = damage / 2;
            if (damage < 1) {
                damage = 1;
            }
            defending = false;
        }
        super.takeDamage(damage);
    }

    // ==================== getter / setter ====================

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public boolean isDefending() {
        return defending;
    }

    public void setDefending(boolean defending) {
        this.defending = defending;
    }
}
