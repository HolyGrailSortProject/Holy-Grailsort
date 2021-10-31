if %1.==debug. (
    gcc -Wall -Og -g test.c -o hgs_dbg.exe
) else (
    gcc -O3 test.c -o hgs_test.exe
)
