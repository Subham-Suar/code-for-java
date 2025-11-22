class Multicatcheg{
    public static void main (String[]args){
        int a,b ;
        try{
             a = Integer.parseInt(args[0]);
             b = Integer.parseInt(args[1]);
             System.out.println(a/b);
        }
        catch(ArithmeticException | NumberFormatException e){
            System.out.println("Invalid user input or second number is Zero");
        }
    }
}