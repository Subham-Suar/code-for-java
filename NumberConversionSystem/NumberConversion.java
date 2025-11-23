import java.util.Scanner ;

class NumberConversion {
    public void decimalTobinary(Scanner sc){
        System.out.print("Enter the decimal number :");
        int Decimal = sc.nextInt();
    
    }
}

    class Use{
        public static void main (String[]args){
            Scanner sc = new Scanner( System.in);
            NumberConversion nc = new NumberConversion();
            nc.decimalTobinary(sc);
        }
    }
