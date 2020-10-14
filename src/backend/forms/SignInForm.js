const ARGUMENTS_NUMBER = 2;
const ARGUMENTS_NAME = [
	'signInFirstName',
	'signInLastName'
]

function dataValidation(args) {
	
	let params = [];

	if(args.length < ARGUMENTS_NUMBER * 2 + 2) { return null; }

	for(let i=2; i<ARGUMENTS_NUMBER * 2 + 2; i+=2) { params[args[i]] = args[i+1]; }

	return params;

}
	
function dataProcessing(params) {
	
	console.log(`Salut ${params['signInFirstName']} ${params['signInLastName']}`);

}
	
function main(args) {
	
	let params;

	if(params = dataValidation(args)) { dataProcessing(params); }

}

main(process.argv);
