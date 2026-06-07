import React from "react";
import { Image, StyleSheet, Text, TouchableOpacity, View } from "react-native";

// componenets in card
interface CardProps {
  icon?: React.ReactNode;
  imageSource?: string;
  title: string;
  description: string;
  buttonText?: string;
  onPress?: () => void;
}

// define components
const Card: React.FC<CardProps> = ({
  icon,
  imageSource,
  title,
  description,
  buttonText,
  onPress,
}) => {
  return (
    <View style={styles.cardContainer}>
      {imageSource && (
        <Image source={{ uri: imageSource }} style={styles.cardImage} />
      )}

      <View style={styles.cardContent}>
        {icon && <View style={styles.cardIcon}>{icon}</View>}
        <Text style={styles.cardTitle}>{title}</Text>
        <Text style={styles.cardDescription}>{description}</Text>

        {buttonText && (
          <TouchableOpacity style={styles.cardButton} onPress={onPress}>
            <Text style={styles.cardButtonText}>{buttonText}</Text>
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
};

//design card
const styles = StyleSheet.create({
  cardContainer: {
    backgroundColor: "#fff",
    borderRadius: 12,
    width: "40%",
    overflow: "hidden",
    marginVertical: 10,
    marginHorizontal: 16,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 6,
    elevation: 5,
    height: 180, //card height
  },
  cardIcon: {},
  cardImage: { width: "100%", height: 100 },
  cardContent: { padding: 16 },
  cardTitle: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#333",
    marginTop: 12,
    textAlign: "center",
  },
  cardDescription: {
    fontSize: 14,
    color: "#666",
    lineHeight: 20,
    marginTop: 16,
  },
  cardButton: {
    backgroundColor: "#007AFF",
    paddingVertical: 10,
    borderRadius: 8,
    alignItems: "center",
  },
  cardButtonText: { color: "#fff", fontSize: 16, fontWeight: "600" },
});

export default Card;
