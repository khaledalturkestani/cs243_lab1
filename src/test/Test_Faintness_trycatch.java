package test;

public class Test_Faintness_trycatch {
  public static void main() {

    try {

      System.out.println("hi");
      return;

      } catch(RuntimeException e) {

    } finally {

      System.out.println("finally");

    }
  }
}


