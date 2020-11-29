# Title     : TODO
# Objective : TODO
# Created by: mxgai
# Created on: 11/11/2020
# install.packages("ggplot2")
#!/usr/bin/env Rscript
# Rscript --vanilla chart_maker.R  Customers 

#install.packages("ggplot2")
#library(ggplot2)
#install.packages("png")
#library(png)

args = commandArgs(trailingOnly=TRUE)
setwd(paste0(getwd(), '/analytics'))
df <- read.csv('histogram.csv')
df$cust_email <- factor(df$cust_email,levels = df$cust_email[order(df$total, decreasing = TRUE)])
title <- ifelse(length(args) == 0, 'Customer', args[0])
my_plot <- ggplot(data=df,aes(x=cust_email,y=total)) + geom_bar(colour = "black",fill = "#DD8888",stat ="identity") + ggtitle(paste("Top", nrow(df), title))
ggsave("test_r_plot.png")
