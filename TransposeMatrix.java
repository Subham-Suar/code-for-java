import java.util.Scanner;

class MatrixTranspose {
    private int[][] matrix;
    private int rows, cols;

    MatrixTranspose(int r, int c) {
        rows = r;
        cols = c;
        matrix = new int[rows][cols];
    }

    void input() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter matrix elements:");
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                matrix[i][j] = sc.nextInt();
    }

    void transpose() {
        // Transpose for square matrix
        if (rows == cols) {
            for (int i = 0; i < rows; i++) {
                for (int j = i + 1; j < cols; j++) {
                    int temp = matrix[i][j];
                    matrix[i][j] = matrix[j][i];
                    matrix[j][i] = temp;
                }
            }
        } else {
            // For non-square
            int[][] temp = new int[cols][rows];
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    temp[j][i] = matrix[i][j];
            matrix = temp;
            int t = rows;
            rows = cols;
            cols = t;
        }
    }

    void display() {
        System.out.println("Matrix:");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++)
                System.out.print(matrix[i][j] + " ");
            System.out.println();
        }
    }
}

public class TransposeMatrix {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter rows: ");
        int r = sc.nextInt();
        System.out.print("Enter columns: ");
        int c = sc.nextInt();

        MatrixTranspose mt = new MatrixTranspose(r, c);
        mt.input();

        System.out.println("Original matrix:");
        mt.display();

        mt.transpose();
        System.out.println("Transposed matrix:");
        mt.display();
    }
}