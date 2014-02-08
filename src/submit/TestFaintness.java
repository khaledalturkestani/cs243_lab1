package submit;

class TestFaintness {
    /**
     * In this method all variables are faint because the final value is never used.
     * Sample out is at src/test/Faintness.out
     */
    void test1() {
        int x = 2;
        int y = x + 2;
        int z = x + y;
        return;
    }

    /*
		In this test, x (in register R1) is faint throughout, y (in register R2) is not faint since it's returned.
     */
     
    int test2() {
		int x = 0;
		int y = 1;
	    for (int i = 0; i < 10; i++) {
	  	    x = x + 1;
		    y = y + 1;
	    }
	    return y;
  	}  
	
	/*
		Test: method-calls. x (R1) and y (R2) should not be faint since we're passing them to the method method(). Note that 
		the compiler copies the values of x and y from R1 and R2 to T6 and T5, respectively, and then passes the temporary 
		registers to method(), so our analysis unfaints T5 and T6, which is the correct behavior. 
	*/
	int test3() {
		int x = 1;
		int y = 2;
		int z = 3;
		return method(x, y);
	}
	
	int method(int x, int y) {
		return -1;
	}
	
	
	/*
	  	Test: nested for-loop. z (R4) is returned so it's not faint after it's defined, and as a result x (R1) and y (R2)
		are also not faint. Note that in the implementation, R4 is correctly unfainted, but not R1 and R2 due to a similar
		reason as in the above example, which is that their values are copied as constants to z's register R4, so R1 and R2
		are not technically fainted by the analysis. 
	*/
	int test4() {
		int x = 1;
		int y = 2;
		int z = x+y;
	    for (int i = 0; i < 10; i++) {
	  	    x = x + 1;
			for (int j = 0; j < 10; j++) {
			    y = y + 1;
			}
	    }
	    return z;	
	}
	
	/*
		Test: if-else (equivalent to try-catch). y (R2) is not faint and x (R1) is faint throughout, which is the case in
		this example.
	*/
	int test5() {
		int x = 1;
		int y = 2;
		if (y > 3) {
			x = x + 1;
		} else {
			y = y - 1;
		}
		return y;
	}
}
