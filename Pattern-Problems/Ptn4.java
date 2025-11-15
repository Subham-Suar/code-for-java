public class Ptn4 {
    public static void main (String[]args){
        for(int i=0; i<7 ; i++){
            //for space
            for(int j=0; j<= 7-i-1; j++){
                System.out.print(" ");
            }

            //for star
            for(int j=0; j<=2*i+1 ; j++){
                System.out.print(" * ");
            }

            //for space
             for(int j=0; j<=7-i-1 ; j++){
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}
 