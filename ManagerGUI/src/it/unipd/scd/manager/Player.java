package it.unipd.scd.manager;

public class Player {
    private int mNumber = 0;
	private int mAttack = 0;
	private int mDefense = 0;
    private int mPower = 0;
    private int mPrecision = 0;
    private int mSpeed = 0;
    private int mTackle = 0;
	private int mGoalKeeping = 0;
    private String mRole;

    public Player (int number, int attack, int defense, int power, int precision, int speed, int tackle, int goal_keeping, String role) {
        mNumber = number;
        mAttack = attack;
        mDefense = defense;
        mPower = power;
        mPrecision = precision;
        mSpeed = speed;
        mTackle = tackle;
        mGoalKeeping = goal_keeping;
        mRole = role;
    }

     /*** GETTER METHODS ***/
    public int getNumber() {
        return mNumber;
    }

    public int getAttack() {
        return mAttack;
    }

    public int getDefense() {
        return mDefense;
    }

    public int getPower() {
        return mPower;
    }

    public int getPrecision() {
        return mPrecision;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public int getTackle() {
        return mTackle;
    }

    public int getGoalKeeping() {
        return mGoalKeeping;
    }

    public String getRole() { return mRole; }

    /*** SETTER METHODS ***/
    public void setNumber(int number) {
        this.mNumber = number;
    }

    public void setAttack(int attack) {
        this.mAttack = attack;
    }

    public void setDefense(int defense) {
        this.mDefense = defense;
    }

    public void setPower(int power) {
        this.mPower = power;
    }

    public void setPrecision(int precision) {
        this.mPrecision = precision;
    }

    public void setSpeed(int speed) {
        this.mSpeed = speed;
    }

    public void setTackle(int tackle) {
        this.mTackle = tackle;
    }

    public void setGoalKeeping(int goal_keeping) {
        this.mGoalKeeping = goal_keeping;
    }

    public void setRole(String role) { mRole = role; }
}
