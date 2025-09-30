package com.example.banking;

public class Account {
    private int balance;

    public Account(int balance) {
        this.balance = balance;
    }


    public void withdrawUnsafe(int amount) {
        if (balance >= amount) { // check
            try {
                // simulate some processing delay
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
            balance -= amount;   // subtract
            System.out.println(Thread.currentThread().getName() +
                    " withdrew " + amount + " (unsafe). Balance now: " + balance);
        } else {
            System.out.println(Thread.currentThread().getName() +
                    " failed (unsafe). Balance: " + balance);
        }
    }

    public synchronized void withdrawSafe(int amount) {
        if (balance >= amount) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}
            balance -= amount;
            System.out.println(Thread.currentThread().getName() +
                    " withdrew " + amount + " (safe). Balance now: " + balance);
        } else {
            System.out.println(Thread.currentThread().getName() +
                    " failed (safe). Balance: " + balance);
        }
    }

    public int getBalance() {
        return balance;
    }

    public static void main(String[] args) throws InterruptedException {
        // First, show unsafe
        System.out.println("=== Unsafe demo ===");
        Account acc1 = new Account(100);
        Thread t1 = new Thread(() -> acc1.withdrawUnsafe(100), "T1");
        Thread t2 = new Thread(() -> acc1.withdrawUnsafe(100), "T2");
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("Final balance (unsafe): " + acc1.getBalance());

        // Now, show safe
        System.out.println("\n=== Safe demo ===");
        Account acc2 = new Account(100);
        Thread t3 = new Thread(() -> acc2.withdrawSafe(100), "T3");
        Thread t4 = new Thread(() -> acc2.withdrawSafe(100), "T4");
        t3.start(); t4.start();
        t3.join(); t4.join();
        System.out.println("Final balance (safe): " + acc2.getBalance());
    }
}