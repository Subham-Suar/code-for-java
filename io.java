import java.util.Scanner;

class Solution {
    public void printNumber(Scanner subham) {
        int num =subham.nextInt();
        System.out.println(+num);
    }
    public static void main (String[]args){
        Scanner sc = new Scanner (System.in);
        Solution s = new Solution();
        s.printNumber(sc);
    }
}