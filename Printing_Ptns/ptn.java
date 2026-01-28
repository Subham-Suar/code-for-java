package Printing_Ptns;

    class Ptn {
        public static void main(String[] args) {
                //for row
               for(int i=0; i<5; i++){
                //for space
                for(int  j=0; j<5-i-1; j++){
                        System.out.print(" ");}
                 //for star
                        for(int k=0; k<2*i+1; k++){
                                System.out.print("*");}
                 //for space
                                for (int  l=0; l<5-i-1; l++) {
                                      System.out.print(" ");  
                                }
                                System.out.println(); 
                        }
                }
               
               } 

