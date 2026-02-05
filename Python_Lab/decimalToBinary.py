def decimal_to_binary(num):
    binary = ""
    while num > 0:
        binary = str(num % 2) + binary
        num = num // 2
    return binary

n = int(input("Enter a decimal number: "))
print("Binary:", decimal_to_binary(n))