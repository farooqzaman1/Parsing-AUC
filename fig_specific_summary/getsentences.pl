#usr/bin/perl
#use warnings;
use strict;
use Cwd;

#Now we will process each text file
my $currentfile = $ARGV[0];

	#Read all the file content line by line
	open (my $input_file, $currentfile) 
	                        or die "Can't open the file : $!" ;
	my @lines = <$input_file>;
	
	
	#Now we will carry out sentence segmentation
	###################################################################
	#This code block extracts each sntence from the text file. 
	
	# $text		The whole text as one variable
	# $abbr1	Abbreviations that do not occur at the end of a sentence
	# $abbr2	Abbreviations that can occur at the end of a sentence
	# @sentence	Data in sentence form stored in an array
		
	my $abbr1="M([rs]|rs|me)|Dr|U\\\.S(\\\.A|)|[aApP]\\\.[mM]|Calif|Fla|D\\\.C|N\\\.Y|Ont|Pa|V[Aa]|[MS][Tt]|Jan|Feb|Mar|Apr|Aug|Sept?|Oct|Nov|Dec|Assoc|	[oO]\\\.[kK]|Co|R\\\.V|Gov|Se[nc]|U\\\.N|\[A-Z\]|i\\\.e|e\\\.g|vs?|Re[pv]|Gen|Univ|Jr|[fF]t|[Ss]gt|[Pp]res|[Pp]rof|[Aa]pprox|[Cc]orp|[Dd]ef";
	
	my $abbr2="D\\\.C";
	
	my $i = 0;
	my $line;
	my $text;
	
	# Main Script
	
	foreach (@lines) 
	{				# Read one line from text
		chomp;
		if ( /^[ \t]*$/ )
		{		# Skip if the line is empty
			next;
		} 
		else 
		{
			$line = $_;		# store each line in $line
			$line =~ s/^[ \t]+//;	# Remove white spaces at the beginning of the line
			$line =~ s/  +/ /g;	# Remove neighboring spaces
			$line =~ s/\t//g;	# Remove tab
			$line =~ s/ +$//;	# Remove spaces at the end of line
			$text .= " ".$line;	# Put together all lines
		}
	}
	
	$text = substr($text,1);	# Remove the space at initial position
	
	$text =~ s/\? /\?\n/g;			# New line at ? space
	$text =~ s/! /!\n/g;			# New line at ! space
	$text =~ s/\.\" /\."\n/g;		# New like at ." space
	$text =~ s/\?\" ([A-Z])/\?\"\n$1/g;	# New line at ?" space capital
	$text =~ s/!\" ([A-Z])/!\"\n$1/g;	# New line at !" space capital
	$text =~ s/(\.\?!)\) /$1\)\n/g;		# New line at .) space
	#Sumit
	$text =~ s/(\)\?!)\. /$1\.\n/g;		# New line at .) space
	$text =~ s/\. ([A-Z])/\.\n$1/g;         # New line at . space capital
	$text =~ s/\. \"/\.\n\"/g;              # New line at . space "
	$text =~ s/\" \"/\"\n\"/g;		# New line between " and "
	$text =~ s/\b($abbr1)\.\n/$1\. /g;	# Delete new line at $abbr1
	$text =~ s/\b($abbr2)\. ([A-Z\"])/$1\n$2/g;	# New line at $abbr2
	
	my @sentence = split ( /\n/, $text);	# Store sentences in the array 
	
	foreach my $out ( @sentence ) {
		if ( $out =~ /\".+\"/ ) {	# Skip if " appears more than once
			$i++;
			next;
		} else {
			$sentence[$i] =~ s/\"//;	# Remove "
			$i++;
		}
	}
	
	#Array @sentence contains all the sentences in the file
#############################################################################################
	
	#Writing sentences in a proper	 format, remove extreanous spaces
	for(my $i=0; $i<$#sentence; $i++)
	{
		if($sentence[$i] =~ m/^(\s+)/)
		{$sentence[$i] =~ s/^(\s+)//;}	
	}
	
	#Write to file
	my $sent_file_name = $ARGV[1];
	open (my $sentence_file, ">$sent_file_name") or die "Can't open the sentences output file, $! \n";
	foreach (@sentence)
	{print $sentence_file $_,"\n";}
	
	#Using Regular Expressions to extract reference sentences
	my @ref_sentence;
	my $ref_sentence_count = 0;
	
	
# report time taken
print "Time taken was ", (time - $^T), " seconds"; 
