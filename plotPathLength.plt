# You can uncomment the following lines to produce a png figure
#set terminal png enhanced
#set output 'plot.png'

set title "Average Path Length"
set xlabel "cycles"
set ylabel "average path length (log)"
set key right top
set logscale y
plot "output-length-Shuffle-c30.txt" title 'Basic Shuffle c = 30' with lines, \
	"output-length-Shuffle-c50.txt" title 'Basic Shuffle c = 50' with lines, \
	"output-length-Random-c30.txt" title 'Random c = 30' with lines, \
	"output-length-Random-c50.txt" title 'Random c = 50' with lines