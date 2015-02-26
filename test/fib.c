int add(int arg1, int arg2) {
	return arg1 + arg2;
}

void fib(int n) {
	int i = 0;
	int prev1 = i;
	int prev2 = i;
	int sum = 0;
	int temp = 0;

	while(i < n) {
		if(sum == 0) {
			printf(0);
			prev1 = 1;
			sum = 1;
		} else {
			sum = add(prev1, prev2);
			printf(sum);
			temp = prev1;
			prev1 = sum;
			prev2 = temp;
		}
		i = add(i, 1);
	}
}

int main() {
	printf("fibonacci sequence up to 10");
	fib(10);
}
