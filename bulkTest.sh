# mvn clean install
# mvn dependency:copy-dependencies
for f in UBTWrapperJena2.5 UBTWrapperBigData UBTWrapperOwlim UBTWrapperSesame2.2 UBTWrapperVirtuoso5.0 UBTWrapperJena2.7 UBTWrapper4Store UBTWrapperSesame2.7 UBTWrapperVirtuoso7.0 UBTWrapperYARS
do
	echo "============================================================================================================="
	echo "============================================================================================================="

	cd $f
	echo "Loading database: $f !!"
	sh load*.sh
	echo "Finished Laoding Database: $f !!"
	echo "Querying database: $f !!"
	sh query*.sh
	echo "Finished querying Database: $f !!"
	cd ..
done