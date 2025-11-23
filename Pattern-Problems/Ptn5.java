public class Ptn5 {
  public void pyramid1(int n){
    for(int i=0; i<n; i++ ){
        for(int j=0 ; j<=i ; j++){
            System.out.print(" * ");
        } 
         System.out.println();
    }

    for(int i=n-1 ; i>0; i--){
        for(int j=1 ; j<=i ; j++){
            System.out.print(" * ");
        } 
         System.out.println();
    }
  }


  public static void main (String[]args){
    Ptn5 p = new Ptn5();
    p.pyramid1(5);
  } 
}
