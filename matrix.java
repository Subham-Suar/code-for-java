  class Matrix{
	private int mat[][];
	static java.util.Scanner sc;

	static{
		sc= new java.util.Scanner(System.in);
	}

	public Matrix(int r, int c){
		mat= new int[r][c];
	}

	public void initialize(){	
		System.out.println("Enter " + mat.length *mat[0].length  + " numbers for (" + mat.length + " X " + mat[0].length + ") Matrix :");
		for(int i= 0; i < mat.length; i++)
			for(int j= 0; j < mat[i].length; j++){
				System.out.print("Enter value for element [" +  i + "][" + j + "] : ");
				mat[i][j]= sc.nextInt();
			}
	}
	
	public Matrix multiply(Matrix m){
		if ( mat[0].length != m.mat.length)
			return null;

		Matrix res=  new Matrix(mat.length, m.mat[0].length);

		for(int i= 0; i < mat.length; i++)
			for(int j= 0; j < m.mat[0].length; j++)
				for(int k= 0; k < m.mat.length; k++)
					res.mat[i][j] += mat[i][k] * m.mat[k][j];

		return res;
	}

	public void display(){
		for(int i= 0; i < mat.length; i++){
			for(int j= 0; j < mat[i].length; j++)
				System.out.print(mat[i][j]+ "   ");
			System.out.println();		
		}
	}
}

class Use{
	public static void main(String[]args)
	{
		Matrix m1= new Matrix(3, 4);
		Matrix m2= new Matrix(4, 2);

		m1.initialize();
		m2.initialize();

		Matrix m3= m1.multiply(m2);		

		System.out.println("First Matrix is : ");
		m1.display();
		System.out.println("\nSecond Matrix is : ");
		m2.display();
		if ( m3 != null)
		{
			System.out.println("\nMultiplied Matrix is : ");
			m3.display();
		}
		else
			System.out.println("Not Multiplied.");
	}
}