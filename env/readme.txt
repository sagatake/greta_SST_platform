###################################################################
# Please create all environment from .yml files in this directory #
###################################################################

##################################################################################
# To create conda virtual environment for SST, you need to run following command #
##################################################################################

conda env create -f pytorch-SST.yaml

##########################
# Other useful materials #
##########################

1) How to export/load conda virtual environment
https://towardsdatascience.com/how-to-export-and-load-anaconda-environments-for-data-science-projects-77dc3b781369

2) Export virtual environment (you need to activate the environment beforehand with "conda activate pytorch-SST")
conda env export > pytorch-SST.yaml

3) Load virtual environment
conda env create -f pytorch-SST.yaml