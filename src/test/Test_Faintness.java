package test;

public class Test_Faintness {
  public void main() {
	  int x = test();
  }
  
  public int test() {
	  int x = 0;
	  int y = 1;
	  for (int i = 0; i < 10; i++) {
	  	  x = x + 1;
		  y = y + 1;
	  }
	  return y;
  }
}
