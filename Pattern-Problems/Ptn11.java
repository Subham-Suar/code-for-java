public class Ptn11 {
    public Ptn11(int n) {

        for (int i = 1; i < n; i++) {
            int start = (i % 2 == 1) ? 1 : 0;

            for (int j = 1; j <= i; j++) {
                System.out.print(start + " ");
                start = 1 - start;

            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Ptn11 p = new Ptn11(6);
    }
}
