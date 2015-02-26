int main(){
	   int a = 0;
	   int target = 10;
	   int result = 0;
	   while (a != target){
			result = target - a;
			if(result >= 5){
				printf("We are far from the target");
			}else{
				printf("We are getting close");
			}
			if(target - 1 == a){
				printf("Target reached");
			}
			a = a + 1;
	   }


	return 0;
}