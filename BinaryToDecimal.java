import java.util.Scanner;

public class BinaryToDecimal {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Ask the user to enter a binary number
        System.out.print("Enter a binary number: ");
        String binary = scanner.nextLine();

        try {
            // Convert binary to decimal
            int decimal = Integer.parseInt(binary, 2);

            // Display the result
            System.out.println("The decimal value is: " + decimal);
        } catch (NumberFormatException e) {
            // Handle invalid input
            System.out.println("Invalid binary number!");
        }

        scanner.close();
    }
}
