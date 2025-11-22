  import java.lang.*;
  class MyThread extends Thread {
    public void run(){
        System.out.println("Thread is Created Using Thread class");
    }
    class SThread implements Runnable {
    public void run(){
        System.out.println("Thread is Created Using Runnable interface");
    }
}
       public static void main (String[]args){
         MyThread t = new MyThread();
         MyThread.SThread sthread = t.new SThread();// first create the instance of  inner class from outer class 
           Thread r = new Thread(sthread);
         t.start(); 
         r.start();  
        }
}



//   class MyThread implements Runnable {
//     public void run(){
//         System.out.println("Thread is Created Using Runnable interface");
//     }
//        public static void main (String[]args){
//          Thread t = new Thread(new MyThread()); // Because MyThread is a Runnable 
//          t.start();   
//         }
// }