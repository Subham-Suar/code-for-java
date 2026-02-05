start = int(input("Enter first 2-digit number: "))
end = int(input("Enter second 2-digit number: "))

n = start
while n <= end:
    if n > 1:
        i = 2
        is_prime = True
        while i <= n // 2:
            if n % i == 0:
                is_prime = False
                break
            i += 1
        if is_prime:
            print(n, end=" ")
    n += 1