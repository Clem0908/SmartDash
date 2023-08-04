function reloadPage() {
	
	window.location.reload();
}

function readFile(file) {
	
	var openFile = file.target.files[0];
	
	if(!file) {return;}
	
	var fileReader = new FileReader();
	var fileReader.onload = function(e) {
			var contents = e.target.result;
			displayContents(contents);
	};
	fileReader.readAsText(openFile);
}

function displayContents(contents) {

	var element = document.getElementById('speedValue');
	element.textContent = contents;
}

document.getElementById('speedFile').addEventListener('change', readFile, false);

setInterval(reloadPage, 500);
