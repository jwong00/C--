char foo() {
	return 'c';
}

float bar(int arg1, int arg2, char arg3) {
	return 1.1;
}

int sum(int arg1, int arg2) {
	return arg1 + arg2;
}

int main() {
	int i = 1;

	char c = foo();
	printf("foo()");
	printf(c);

	float f = bar(0, 1, 'c');
	printf("bar(0, 1, 'c')");
	printf(f);

	int s = sum(i, 2);
	printf("sum(i, 2)");
	printf(s);
}
