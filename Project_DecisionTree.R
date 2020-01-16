dataset <- read.csv(file = "student-mat.csv", header = TRUE, sep = ',', na.strings = "?")

set.seed(113)
train.idx1 <- sample(1:nrow(dataset), size = 0.8 * nrow(dataset), replace = FALSE)

train.set1 <- dataset[train.idx1,]
test.set1 <- dataset[-train.idx1,]

train.idx2 <- sample(1:nrow(train.set1), size = (2 / 3) * nrow(train.set1), replace = FALSE)

train.set2 <- train.set1[train.idx2,]
test.set2 <- train.set1[-train.idx2,]

train.idx3 <- sample(1:nrow(train.set2), size = 0.75 * nrow(train.set2), replace = FALSE)

train.set3 <- train.set2[train.idx3,]
test.set3 <- train.set2[-train.idx3,]

train.idx4 <- sample(1:nrow(train.set3), size = 0.5 * nrow(train.set3), replace = FALSE)

test.set4 <- train.set3[train.idx4,]
test.set5 <- train.set3[-train.idx4,]

train.set1 <- rbind(test.set2,test.set3,test.set4,test.set5)
train.set2 <- rbind(test.set1,test.set3,test.set4,test.set5)
train.set3 <- rbind(test.set1,test.set2,test.set4,test.set5)
train.set4 <- rbind(test.set1,test.set2,test.set3,test.set5)
train.set5 <- rbind(test.set1,test.set2,test.set3,test.set4)


library("tree")
# tree(formula, data, weights)

tree.model1 <- tree(as.factor(Walc) ~ ., data = train.set1, split = c("deviance", "gainratio"))

tree.pred1 <- predict(tree.model1, newdata = test.set1, type = "class") 

tree.model2 <- tree(as.factor(Walc) ~ ., data = train.set2)

tree.pred2 <- predict(tree.model2, newdata = test.set2, type = "class") 

tree.model3 <- tree(as.factor(Walc) ~ ., data = train.set3)

tree.pred3 <- predict(tree.model3, newdata = test.set3, type = "class") 

tree.model4 <- tree(as.factor(Walc) ~ ., data = train.set4)

tree.pred4 <- predict(tree.model4, newdata = test.set4, type = "class") 

tree.model5 <- tree(as.factor(Walc) ~ ., data = train.set5)

tree.pred5 <- predict(tree.model5, newdata = test.set5, type = "class") 

# table(Predictions = tree.pred, Actual = test.set$Walc)
# accuracy
a1 <- mean(tree.pred1 == test.set1$Walc)
a2 <- mean(tree.pred2 == test.set2$Walc)
a3 <- mean(tree.pred3 == test.set3$Walc)
a4 <- mean(tree.pred4 == test.set4$Walc)
a5 <- mean(tree.pred5 == test.set5$Walc)
mean(a1,a2,a3,a4,a5)
