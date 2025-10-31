import java.util.Scanner;
class Matrix{
      	 int r,c;
      	 int matrix[][]= new Matrix(int row,int col) ;
	Scanner sc = new Scanner(System.in);

     Matrix(){
 	r = row;
	c = col;
        matrix[][] = new int[r][c];
      intialization();
  }
	public void intialization(){
	    System.out.println("Enter the values for" +row"X"+col "matrix");
     		for(int i=1; i<=row;i++){
      		 for(int j=1; j<=col;j++){
 		    matrix [i][j] = sc.nextInt();
			
		 }
             }
         }
  
   


}

class Use{
   public static void main(String[]args){


	
  }
}	