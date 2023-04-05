const toggleSidebar = () => {
	
	if($(".sidebar").is(":visible")){
		//band karna hai
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "2%");
	}else{
		//dikhana hai
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
	
};
